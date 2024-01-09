package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

public class ActiveQuestRepositoryIT {

  private DataSource dataSource;

  private ActiveQuestRepository activeQuestRepository;
  private PlayersRepository playersRepository;
  private QuestsRepository questsRepository;

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:16.1-alpine"
  );

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
        postgres.getDatabaseName(), postgres.getUsername(), postgres.getPassword());

    activeQuestRepository = new ActiveQuestRepository(Logger.getLogger(this.getClass().getName()),
        dataSource, concurrencyHandler);
    playersRepository = new PlayersRepository(Logger.getLogger(this.getClass().getName()),
        dataSource, concurrencyHandler);
    questsRepository = new QuestsRepository(Logger.getLogger(this.getClass().getName()), dataSource,
        concurrencyHandler);

    // Create tables to avoid foreign key errors
    playersRepository.createTable().join();
    questsRepository.createTable().join();
    activeQuestRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
    activeQuestRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() creates active_quest table")
  public void testCreateTable() {
    // The table is already being created in setUp(), so we just check if it exists
    activeQuestRepository.doesTableExists().thenAccept(Assertions::assertTrue).join();
  }

  @Test
  @DisplayName("insert() inserts active quest into database")
  public void testInsert_SuccessfullyInsertsIntoDatabaseAndReturnsId() {

    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    // Insert a player and a quest to avoid foreign key errors
    playersRepository.insert(
        new PlayerModel(id.playerId())
    ).join();
    questsRepository.insert(
        new QuestModel(1, "Test Quest", "Test Quest Description", "",
            "", "")
    ).join();

    PlayerQuestKey playerQuestKey = activeQuestRepository.insert(
        new ActiveQuestModel(id.playerId(), id.questId(), 1, 0F)
    ).join();

    Assertions.assertEquals(playerQuestKey, id);

    activeQuestRepository.findById(id).thenAccept(quest -> {
      Assertions.assertTrue(quest.isPresent());
      Assertions.assertEquals(id.playerId(), quest.get().playerId());
      Assertions.assertEquals(id.questId(), quest.get().questId());
      Assertions.assertEquals(1, quest.get().completedGoals());
      Assertions.assertEquals(0F, quest.get().progress());
    }).join();

  }

  @Test
  @DisplayName("insert() throws when player does not exist")
  public void testDelete_RemovesActiveQuestFromDatabase() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    // Insert a player, a quest and an active quest to avoid foreign key errors
    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();
    activeQuestRepository.insert(new ActiveQuestModel(id.playerId(), id.questId(), 1, 0F)).join();

    activeQuestRepository.delete(id).join();

    activeQuestRepository.findById(id)
        .thenAccept(quest -> Assertions.assertFalse(quest.isPresent())).join();
  }

  @Test
  @DisplayName("delete() does not throw when active quest does not exist")
  public void testDelete_DoesNotThrowWhenActiveQuestDoesNotExist() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    Assertions.assertDoesNotThrow(() -> activeQuestRepository.delete(id).join());

  }

  @Test
  @DisplayName("findById() returns active quest when it exists in database")
  public void testFindById_ReturnsActiveQuestWhenItExists() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    // Insert a player, a quest and an active quest to avoid foreign key errors
    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();
    activeQuestRepository.insert(new ActiveQuestModel(id.playerId(), id.questId(), 1, 0F)).join();

    activeQuestRepository.findById(id).thenAccept(quest -> {
      Assertions.assertTrue(quest.isPresent());
      Assertions.assertEquals(id.playerId(), quest.get().playerId());
      Assertions.assertEquals(id.questId(), quest.get().questId());
      Assertions.assertEquals(1, quest.get().completedGoals());
      Assertions.assertEquals(0F, quest.get().progress());
    }).join();
  }

  @Test
  @DisplayName("findById() returns empty when active quest does not exist")
  public void testFindById_ReturnsEmptyWhenActiveQuestDoesNotExist() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    activeQuestRepository.findById(id)
        .thenAccept(activeQuest -> Assertions.assertFalse(activeQuest.isPresent())).join();
  }

}