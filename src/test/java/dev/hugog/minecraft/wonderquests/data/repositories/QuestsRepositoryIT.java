package dev.hugog.minecraft.wonderquests.data.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class QuestsRepositoryIT {

  private DataSource dataSource;
  private QuestsRepository questsRepository;

  final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine");

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

    questsRepository = new QuestsRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);

    QuestObjectivesRepository questObjectivesRepository = new QuestObjectivesRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);

    QuestRewardsRepository questRewardsRepository = new QuestRewardsRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);

    QuestRequirementsRepository questRequirementsRepository = new QuestRequirementsRepository(
        Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler
    );

    questsRepository.createTable().join();
    questObjectivesRepository.createTable().join();
    questRewardsRepository.createTable().join();
    questRequirementsRepository.createTable().join();

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

  }

  @AfterEach
  void tearDown() {
    questsRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a quest table when it does not exist")
  public void createTable_CreatesQuestTableWhenNotExists() {
    assertTrue(questsRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the quest when it exists in the database")
  public void findById_ReturnsQuestWhenExists() {

    questsRepository.insert(questModel).join();

    questsRepository.findById(questModel.id()).thenAccept(quest -> {
      assertTrue(quest.isPresent());
      assertEquals(questModel, quest.get());
    }).join();

  }

  @Test
  @DisplayName("findById() returns empty when the quest does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {

    questsRepository.findById(questModel.id())
        .thenAccept(quest -> assertFalse(quest.isPresent())).join();

  }

  @Test
  @DisplayName("insert() successfully inserts a quest into the database")
  public void insert_SuccessfullyInsertsQuestIntoDatabase() {

    questsRepository.insert(questModel).join();

    questsRepository.findById(questModel.id())
        .thenAccept(quest -> assertTrue(quest.isPresent())).join();

  }

  @Test
  @DisplayName("delete() successfully deletes a quest from the database")
  public void delete_DeletesQuestFromDatabase() {

    questsRepository.insert(questModel).join();

    questsRepository.delete(questModel.id()).join();

    questsRepository.findById(questModel.id())
        .thenAccept(quest -> assertFalse(quest.isPresent())).join();

  }

  @Test
  @DisplayName("findAll() returns all quests in the database")
  public void findAll_ReturnsAllQuestsInDatabase() {

    QuestModel questModel1 = new QuestModel(1, "Test Name", "Test Description",
        "Test Opening Msg", "Test Closing Msg", "Test Item", 1,
        null, new HashSet<>(), new HashSet<>());
    QuestModel questModel2 = new QuestModel(2, "Test Name", "Test Description",
        "Test Opening Msg", "Test Closing Msg", "Test Item", 1,
        null, new HashSet<>(), new HashSet<>());

    questsRepository.insert(questModel1).join();
    questsRepository.insert(questModel2).join();

    questsRepository.findAll()
        .thenAccept(quests -> {
          assertEquals(2, quests.size());
          assertTrue(quests.containsAll(Set.of(questModel1, questModel2)));
        }).join();

  }

  @Test
  @DisplayName("findAllInInterval() returns all quests in the specified interval")
  public void findAllInInterval_ReturnsAllQuestsInSpecifiedInterval() {

    QuestModel questModel1 = new QuestModel(1, "Test Name", "Test Description",
        "Test Opening Msg", "Test Closing Msg", "Test Item", 1,
        null, new HashSet<>(), new HashSet<>());
    QuestModel questModel2 = new QuestModel(2, "Test Name", "Test Description",
        "Test Opening Msg", "Test Closing Msg", "Test Item", 1,
        null, new HashSet<>(), new HashSet<>());
    QuestModel questModel3 = new QuestModel(3, "Test Name", "Test Description",
        "Test Opening Msg", "Test Closing Msg", "Test Item", 1,
        null, new HashSet<>(), new HashSet<>());

    questsRepository.insert(questModel1).join();
    questsRepository.insert(questModel2).join();
    questsRepository.insert(questModel3).join();

    questsRepository.findAllInInterval(0, 2)
        .thenAccept(quests -> {
          assertEquals(2, quests.size());
          Assertions.assertThat(quests).containsExactlyInAnyOrder(questModel1, questModel2);
        }).join();

  }

}