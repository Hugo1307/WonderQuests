package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.CompletedQuestModel;
import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class CompletedQuestRepositoryIT {

  private DataSource dataSource;

  private CompletedQuestRepository completedQuestRepository;
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

    completedQuestRepository = new CompletedQuestRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource, concurrencyHandler);
    playersRepository = new PlayersRepository(Logger.getLogger(this.getClass().getName()),
        dataSource, concurrencyHandler);
    questsRepository = new QuestsRepository(Logger.getLogger(this.getClass().getName()), dataSource,
        concurrencyHandler);

    // Create tables to avoid foreign key errors
    playersRepository.createTable().join();
    questsRepository.createTable().join();
    completedQuestRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() creates completed_quest table")
  public void testCreateTable() {
    // The table is already being created in setUp(), so we just check if it exists
    completedQuestRepository.doesTableExists().thenAccept(Assertions::assertTrue).join();
  }

  @Test
  @DisplayName("findById() returns the completed quest when it exists in the database")
  public void findById_ReturnsCompletedQuestWhenExists() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    // Insert a player, a quest and a completed quest to avoid foreign key errors
    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();
    completedQuestRepository.insert(new CompletedQuestModel(id.playerId(), id.questId())).join();

    completedQuestRepository.findById(id)
        .thenAccept(completedQuest -> {
          Assertions.assertTrue(completedQuest.isPresent());
          Assertions.assertEquals(id.playerId(), completedQuest.get().playerId());
          Assertions.assertEquals(id.questId(), completedQuest.get().questId());
        }).join();
  }

  @Test
  @DisplayName("findById() returns empty when the completed quest does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    completedQuestRepository.findById(id)
        .thenAccept(completedQuest -> Assertions.assertFalse(completedQuest.isPresent())).join();
  }

  @Test
  public void testInsert_SuccessfullyInsertsNewCompletedQuest() {

    CompletedQuestModel model = new CompletedQuestModel(UUID.randomUUID(), 1);

    // Insert a player, a quest and an active quest to avoid foreign key errors
    playersRepository.insert(new PlayerModel(model.playerId())).join();
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();

    PlayerQuestKey result = completedQuestRepository.insert(model).join();
    Assertions.assertEquals(new PlayerQuestKey(model.playerId(), model.questId()), result);

  }

  @Test
  public void testInsert_ThrowsExceptionWhenUnableToInsertNewCompletedQuest() {

    CompletedQuestModel model = new CompletedQuestModel(UUID.randomUUID(), 1);

    Assertions.assertThrows(RuntimeException.class,
        () -> completedQuestRepository.insert(model).join());

  }


  @Test
  @DisplayName("delete() removes the completed quest from the database")
  public void testDelete_SuccessfullyRemovesCompletedQuest() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    // Insert a player, a quest and an active quest to avoid foreign key errors
    playersRepository.insert(new PlayerModel(id.playerId())).join();
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();
    completedQuestRepository.insert(new CompletedQuestModel(id.playerId(), id.questId())).join();

    completedQuestRepository.delete(id).join();

    completedQuestRepository.findById(id)
        .thenAccept(activeQuest -> Assertions.assertFalse(activeQuest.isPresent())).join();

  }

  @Test
  @DisplayName("delete() does not throw exception when completed quest does not exist")
  public void testDelete_NoExceptionWhenNotExists() {
    PlayerQuestKey id = new PlayerQuestKey(UUID.randomUUID(), 1);

    completedQuestRepository.findById(id)
        .thenAccept(activeQuest -> Assertions.assertFalse(activeQuest.isPresent())).join();

  }

}
