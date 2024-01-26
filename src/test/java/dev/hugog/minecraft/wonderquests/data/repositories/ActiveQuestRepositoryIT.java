package dev.hugog.minecraft.wonderquests.data.repositories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class ActiveQuestRepositoryIT {

  private DataSource dataSource;
  private ActiveQuestRepository activeQuestRepository;

  private QuestsRepository questsRepository;
  private PlayersRepository playersRepository;

  final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine");

  private PlayerQuestKey id;
  private QuestModel questModel;

  @BeforeAll
  static void setUpAll() {
    postgres.start();
  }

  @AfterAll
  static void tearDownAll() {
    postgres.stop();
  }

  @BeforeEach
  void setUp() {

    ConcurrencyHandler concurrencyHandler = new ConcurrencyHandler();

    dataSource = new DataSource(Logger.getLogger(this.getClass().getName()));
    dataSource.initDataSource(postgres.getHost(), postgres.getFirstMappedPort().toString(),
        postgres.getDatabaseName(), postgres.getUsername(), postgres.getPassword(), 5);

    activeQuestRepository = new ActiveQuestRepository(Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);
    questsRepository = new QuestsRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);
    playersRepository = new PlayersRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);

    id = new PlayerQuestKey(UUID.randomUUID(), 1);
    questModel = new QuestModel(id.questId(),
        "Test Quest",
        "Test Quest Description",
        "",
        "",
        "",
        0,
        null,
        new HashSet<>(),
        new HashSet<>());

    questsRepository.createTable().join();
    playersRepository.createTable().join();

    activeQuestRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
    questsRepository.deleteTable().join();
    playersRepository.deleteTable().join();
    activeQuestRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates an active quest table when it does not exist")
  public void createTable_CreatesActiveQuestTableWhenNotExists() {
    assertTrue(activeQuestRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the active quest when it exists in the database")
  public void findById_ReturnsActiveQuestWhenExists() {

    long startedAt = System.currentTimeMillis();

    ActiveQuestModel activeQuestModel = new ActiveQuestModel(id.playerId(), id.questId(), 1.0f,
        0.5f, startedAt, questModel);

    // Just insert something to avoid foreign key errors
    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(questModel).join();

    activeQuestRepository.insert(activeQuestModel).join();

    activeQuestRepository.findById(id).thenAccept(activeQuest -> {
      assertTrue(activeQuest.isPresent());
      assertEquals(activeQuest.get(), activeQuestModel);
    }).join();

  }

  @Test
  @DisplayName("findById() returns empty when the active quest does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {

    activeQuestRepository.findById(id)
        .thenAccept(activeQuest -> assertFalse(activeQuest.isPresent())).join();

  }

  @Test
  @DisplayName("insert() successfully inserts an active quest into the database")
  public void insert_SuccessfullyInsertsActiveQuestIntoDatabase() {

    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(questModel).join();

    activeQuestRepository.insert(
        new ActiveQuestModel(id.playerId(), id.questId(), 1.0f, 0.5f, System.currentTimeMillis(),
            null)).join();

    activeQuestRepository.findById(id)
        .thenAccept(activeQuest -> assertTrue(activeQuest.isPresent())).join();

  }

  @Test
  @DisplayName("delete() successfully deletes an active quest from the database")
  public void delete_DeletesActiveQuestFromDatabase() {

    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(questModel).join();

    ActiveQuestModel activeQuestModel = new ActiveQuestModel(
        id.playerId(),
        id.questId(),
        1.0f,
        0.5f,
        System.currentTimeMillis(),
        questModel
    );

    activeQuestRepository.insert(activeQuestModel).join();

    activeQuestRepository.delete(id).join();

    activeQuestRepository.findById(id)
        .thenAccept(activeQuest -> assertFalse(activeQuest.isPresent())).join();

  }

  @Test
  @DisplayName("delete() does not throw when active quest does not exist")
  public void delete_DoesNotThrowWhenActiveQuestDoesNotExist() {

    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);
    assertDoesNotThrow(() -> activeQuestRepository.delete(id).join());

  }

  @Test
  @DisplayName("findAllByPlayerId() returns all active quests for a given player id")
  public void findAllByPlayerId_ReturnsAllActiveQuestsForGivenPlayerId() {

    QuestModel questModel2 = new QuestModel(
        2,
        "Test Quest 2",
        "Test Quest Description",
        "",
        "",
        "",
        0,
        null,
        new HashSet<>(),
        new HashSet<>()
    );

    ActiveQuestModel activeQuestModel1 = new ActiveQuestModel(id.playerId(), 1, 1.0f, 0.5f,
        System.currentTimeMillis(), questModel);
    ActiveQuestModel activeQuestModel2 = new ActiveQuestModel(id.playerId(), 2, 1.0f, 0.5f,
        System.currentTimeMillis(), questModel2);

    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(questModel).join();
    questsRepository.insert(questModel2).join();

    activeQuestRepository.insert(activeQuestModel1).join();
    activeQuestRepository.insert(activeQuestModel2).join();

    activeQuestRepository.findAllByPlayerId(id.playerId())
        .thenAccept(activeQuests -> {
          assertEquals(2, activeQuests.size());
          assertTrue(activeQuests.containsAll(Set.of(activeQuestModel1, activeQuestModel2)));
        }).join();

  }

  @Test
  @DisplayName("findAllByPlayerId() returns empty set when no active quests exist for a given player id")
  public void findAllByPlayerId_ReturnsEmptySetWhenNoActiveQuestsExistForGivenPlayerId() {

    UUID playerId = UUID.randomUUID();

    activeQuestRepository.findAllByPlayerId(playerId)
        .thenAccept(activeQuests -> assertTrue(activeQuests.isEmpty())).join();

  }

  @Test
  @DisplayName("save() successfully updates an active quest in the database")
  public void save_UpdatesActiveQuestSuccessfully() {

    ActiveQuestModel activeQuestModel = new ActiveQuestModel(id.playerId(), id.questId(), 1.0f,
        0.5f, System.currentTimeMillis(), questModel);

    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(questModel).join();

    activeQuestRepository.insert(activeQuestModel).join();

    activeQuestRepository.save(activeQuestModel).join();

    activeQuestRepository.findById(id)
        .thenAccept(activeQuest -> {
          assertTrue(activeQuest.isPresent());
          assertEquals(0.5f, activeQuest.get().progress());
        }).join();

  }

  @Test
  @DisplayName("save() does not update an active quest when it does not exist in the database")
  public void save_DoesNotUpdateActiveQuestWhenNotExists() {

    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    ActiveQuestModel activeQuestModel = new ActiveQuestModel(id.playerId(), id.questId(), 1.0f,
        0.5f, System.currentTimeMillis(), null);

    assertFalse(activeQuestRepository.save(activeQuestModel).join());

  }

}