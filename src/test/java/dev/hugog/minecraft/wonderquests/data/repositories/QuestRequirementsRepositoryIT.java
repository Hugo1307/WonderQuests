package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.logging.Logger;

public class QuestRequirementsRepositoryIT {

  private DataSource dataSource;
  private QuestRequirementsRepository questRequirementsRepository;
  final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine");

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

    QuestsRepository questsRepository = new QuestsRepository(
        Logger.getLogger(this.getClass().getName()), dataSource,
        concurrencyHandler);
    questRequirementsRepository = new QuestRequirementsRepository(
        Logger.getLogger(this.getClass().getName()), dataSource, concurrencyHandler);

    // Create quest table before creating quest objectives table because of foreign key
    questsRepository.createTable().join();
    questRequirementsRepository.createTable().join();

    // Insert a quest to avoid foreign key errors in further tests
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", "", 0, null, null, null))
        .join();

  }

  @AfterEach
  void tearDown() {
    questRequirementsRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a quest requirement table when it does not exist")
  public void createTable_CreatesQuestRequirementTableWhenNotExists() {
    Assertions.assertTrue(questRequirementsRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the quest requirement when it exists in the database")
  public void findById_ReturnsQuestRequirementWhenExists() {
    int id = 1;

    QuestRequirementModel questRequirementModel = new QuestRequirementModel(id, 1, "Test Type",
        "Test String Value", 1.0f);

    questRequirementsRepository.insert(questRequirementModel).join();

    questRequirementsRepository.findById(id).thenAccept(questRequirement -> {
      Assertions.assertTrue(questRequirement.isPresent());
      Assertions.assertEquals(id, questRequirement.get().id());
      Assertions.assertEquals(questRequirement.get(), questRequirementModel);
    }).join();
  }

  @Test
  @DisplayName("findById() returns empty when the quest requirement does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {
    int id = 1;

    questRequirementsRepository.findById(id)
        .thenAccept(questRequirement -> Assertions.assertFalse(questRequirement.isPresent()))
        .join();
  }

  @Test
  @DisplayName("insert() successfully inserts a quest requirement into the database")
  public void insert_SuccessfullyInsertsQuestRequirementIntoDatabase() {
    int id = 1;

    questRequirementsRepository.insert(
        new QuestRequirementModel(id, 1, "Test Type", "Test String Value", 1.0f)).join();

    questRequirementsRepository.findById(id)
        .thenAccept(questRequirement -> Assertions.assertTrue(questRequirement.isPresent())).join();
  }

  @Test
  @DisplayName("delete() successfully deletes a quest requirement from the database")
  public void delete_DeletesQuestRequirementFromDatabase() {
    int id = 1;

    questRequirementsRepository.insert(
        new QuestRequirementModel(id, 1, "Test Type", "Test String Value", 1.0f)).join();

    questRequirementsRepository.delete(id).join();

    questRequirementsRepository.findById(id)
        .thenAccept(questRequirement -> Assertions.assertFalse(questRequirement.isPresent()))
        .join();
  }

  @Test
  @DisplayName("delete() does not throw when quest requirement does not exist")
  public void delete_DoesNotThrowWhenQuestRequirementDoesNotExist() {
    int id = 1;
    Assertions.assertDoesNotThrow(() -> questRequirementsRepository.delete(id).join());
  }

  @Test
  @DisplayName("findAllByQuestId() returns all quest requirements for a given quest id")
  public void findAllByQuestId_ReturnsAllQuestRequirementsForGivenQuestId() {
    int questId = 1;

    QuestRequirementModel questRequirementModel1 = new QuestRequirementModel(1, questId,
        "Test Type 1", "Test String Value 1", 1.0f);
    QuestRequirementModel questRequirementModel2 = new QuestRequirementModel(2, questId,
        "Test Type 2", "Test String Value 2", 2.0f);

    questRequirementsRepository.insert(questRequirementModel1).join();
    questRequirementsRepository.insert(questRequirementModel2).join();

    questRequirementsRepository.findAllByQuestId(questId)
        .thenAccept(questRequirements -> {
          Assertions.assertEquals(2, questRequirements.size());
          org.assertj.core.api.Assertions.assertThat(questRequirements)
              .containsAll(List.of(questRequirementModel1, questRequirementModel2));
        }).join();

  }

  @Test
  @DisplayName("findAllByQuestId() returns empty list when no quest requirements exist for a given quest id")
  public void findAllByQuestId_ReturnsEmptyListWhenNoQuestRequirementsExist() {

    int questId = 1;
    questRequirementsRepository.findAllByQuestId(questId)
        .thenAccept(questObjectives -> Assertions.assertTrue(questObjectives.isEmpty())).join();

  }

}