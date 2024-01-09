package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class QuestObjectivesRepositoryIT {

  private DataSource dataSource;

  private QuestObjectivesRepository questObjectivesRepository;

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine");

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

    QuestsRepository questsRepository = new QuestsRepository(
        Logger.getLogger(this.getClass().getName()), dataSource,
        concurrencyHandler);
    questObjectivesRepository = new QuestObjectivesRepository(
        Logger.getLogger(this.getClass().getName()), dataSource,
        concurrencyHandler);

    // Create quest table before creating quest objectives table because of foreign key
    questsRepository.createTable().join();
    questObjectivesRepository.createTable().join();

    // Insert a quest to avoid foreign key errors
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();

  }

  @AfterEach
  void tearDown() {
    questObjectivesRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a quest_objective table when it does not exist")
  public void createTable_CreatesQuestObjectiveTableWhenNotExists() {
    Assertions.assertTrue(questObjectivesRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("insert() successfully inserts a quest objective into the database")
  public void insert_SuccessfullyInsertsQuestObjectiveIntoDatabase() {
    int id = 1;
    QuestObjectiveModel questObjectiveModel = new QuestObjectiveModel(id, 1, "Test Type",
        "Test Value", 1.0f);

    questObjectivesRepository.insert(questObjectiveModel).join();

    // Check if the quest objective exists in the database
    questObjectivesRepository.findById(id).thenAccept(quest -> {
      Assertions.assertTrue(quest.isPresent());
      Assertions.assertEquals(questObjectiveModel, quest.get());
    }).join();
  }

  @Test
  @DisplayName("delete() successfully deletes a quest objective from the database")
  public void delete_DeletesQuestObjectiveFromDatabase() {

    int id = 1;

    questObjectivesRepository.insert(
        new QuestObjectiveModel(id, 1, "Test Type", "Test Value", 1.0f)).join();
    questObjectivesRepository.delete(id).join();

    // Check if the quest objective no longer exists in the database
    questObjectivesRepository.findById(id)
        .thenAccept(questObjective -> Assertions.assertFalse(questObjective.isPresent())).join();

  }

  @Test
  @DisplayName("delete() does not affect other quest objectives in the database")
  public void delete_DoesNotAffectOtherQuestObjectives() {

    int id1 = 1;
    int id2 = 2;

    questObjectivesRepository.insert(
        new QuestObjectiveModel(id1, 1, "Test Type", "Test Value", 1.0f)).join();
    questObjectivesRepository.insert(
        new QuestObjectiveModel(id2, 1, "Test Type", "Test Value", 1.0f)).join();

    questObjectivesRepository.delete(id1).join();

    // Check if the second quest objective still exists in the database
    questObjectivesRepository.findById(id2)
        .thenAccept(questObjective -> Assertions.assertTrue(questObjective.isPresent())).join();

  }

  @Test
  @DisplayName("delete() doesn't throw exception when the quest objective does not exist in the database")
  public void delete_DoesNotThrowsExceptionWhenNotExists() {

    int id = 1;
    Assertions.assertDoesNotThrow(() -> questObjectivesRepository.delete(id).join());

  }

  @Test
  @DisplayName("findById() returns the quest objective when it exists in the database")
  public void findById_ReturnsQuestObjectiveWhenExists() {
    int id = 1;

    QuestObjectiveModel questObjectiveModel = new QuestObjectiveModel(id, 1, "Test Type",
        "Test Value", 1.0f);

    questObjectivesRepository.insert(questObjectiveModel).join();

    questObjectivesRepository.findById(id)
        .thenAccept(questObjective -> {
          Assertions.assertTrue(questObjective.isPresent());
          Assertions.assertEquals(id, questObjective.get().id());
          Assertions.assertEquals(questObjectiveModel, questObjective.get());
        }).join();
  }

  @Test
  @DisplayName("findById() returns empty when the quest objective does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {
    int id = 1;

    questObjectivesRepository.findById(id)
        .thenAccept(questObjective -> Assertions.assertFalse(questObjective.isPresent())).join();
  }

  @Test
  @DisplayName("findAllByQuestId() returns all quest objectives for a given quest id")
  public void findAllByQuestId_ReturnsAllQuestObjectivesForGivenQuestId() {

    int questId = 1;

    questObjectivesRepository.insert(
        new QuestObjectiveModel(1, questId, "Test Type", "Test Value", 1.0f)).join();
    questObjectivesRepository.insert(
        new QuestObjectiveModel(2, questId, "Test Type 2", "Test Value 2", 2.0f)).join();

    questObjectivesRepository.findAllByQuestId(questId)
        .thenAccept(questObjectives -> {
          Assertions.assertEquals(2, questObjectives.size());
          Assertions.assertEquals(questId, questObjectives.get(0).questId());
          Assertions.assertEquals(questId, questObjectives.get(1).questId());
        }).join();

  }

  @Test
  @DisplayName("findAllByQuestId() returns empty list when no quest objectives exist for a given quest id")
  public void findAllByQuestId_ReturnsEmptyListWhenNoQuestObjectivesExist() {

    int questId = 1;
    questObjectivesRepository.findAllByQuestId(questId)
        .thenAccept(questObjectives -> Assertions.assertTrue(questObjectives.isEmpty())).join();

  }

}