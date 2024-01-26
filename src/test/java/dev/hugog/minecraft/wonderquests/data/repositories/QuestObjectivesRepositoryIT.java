package dev.hugog.minecraft.wonderquests.data.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import java.util.HashSet;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class QuestObjectivesRepositoryIT {

  private DataSource dataSource;

  private QuestsRepository questsRepository;

  private QuestObjectivesRepository questObjectivesRepository;

  final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine");

  private QuestModel questModel;
  private QuestObjectiveModel questObjectiveModel;

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

    questsRepository = new QuestsRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler
    );

    questObjectivesRepository = new QuestObjectivesRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler
    );

    questModel = new QuestModel(
        1,
        "Test Name",
        "Test Description",
        "Opening Message",
        "Closing Message",
        "sand", 1,
        null,
        new HashSet<>(),
        new HashSet<>()
    );

    questObjectiveModel = new QuestObjectiveModel(
        1,
        questModel.id(),
        "Test Type",
        "Test Value",
        1.0f
    );

    questsRepository.createTable().join();
    questObjectivesRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
    questObjectivesRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a quest_objective table when it does not exist")
  public void createTable_CreatesQuestObjectiveTableWhenNotExists() {
    assertTrue(questObjectivesRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the quest objective when it exists in the database")
  public void findById_ReturnsQuestObjectiveWhenExists() {

    questsRepository.insert(questModel).join();
    questObjectivesRepository.insert(questObjectiveModel).join();

    questObjectivesRepository.findById(questObjectiveModel.id())
        .thenAccept(questObjective -> {
          assertTrue(questObjective.isPresent());
          assertEquals(questObjective.get(), questObjectiveModel);
        }).join();

  }

  @Test
  @DisplayName("findById() returns empty when the quest objective does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {

    questObjectivesRepository.findById(questObjectiveModel.id())
        .thenAccept(questObjective -> assertFalse(questObjective.isPresent())).join();

  }

  @Test
  @DisplayName("insert() successfully inserts a quest objective into the database")
  public void insert_SuccessfullyInsertsQuestObjectiveIntoDatabase() {

    questsRepository.insert(questModel).join();

    questObjectivesRepository.insert(questObjectiveModel).join();

    questObjectivesRepository.findById(questObjectiveModel.id())
        .thenAccept(questObjective -> assertTrue(questObjective.isPresent())).join();

  }

  @Test
  @DisplayName("delete() successfully deletes a quest objective from the database")
  public void delete_DeletesQuestObjectiveFromDatabase() {

    questsRepository.insert(questModel).join();
    questObjectivesRepository.insert(questObjectiveModel).join();

    questObjectivesRepository.delete(questObjectiveModel.id()).join();

    questObjectivesRepository.findById(questObjectiveModel.id())
        .thenAccept(questObjective -> assertFalse(questObjective.isPresent())).join();

  }

  @Test
  @DisplayName("findByQuestId() returns the quest objective when it exists in the database")
  public void findByQuestId_ReturnsQuestObjectiveWhenExists() {

    questsRepository.insert(questModel).join();
    questObjectivesRepository.insert(questObjectiveModel).join();

    questObjectivesRepository.findByQuestId(questModel.id()).thenAccept(questObjective -> {
      assertTrue(questObjective.isPresent());
      assertEquals(questObjective.get(), questObjectiveModel);
    }).join();

  }

  @Test
  @DisplayName("findByQuestId() returns empty when the quest objective does not exist in the database")
  public void findByQuestId_ReturnsEmptyWhenNotExists() {

    questObjectivesRepository.findByQuestId(questModel.id())
        .thenAccept(questObjective -> assertFalse(questObjective.isPresent())).join();

  }

}