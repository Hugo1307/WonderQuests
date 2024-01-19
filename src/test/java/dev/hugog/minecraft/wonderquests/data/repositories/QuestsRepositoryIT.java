package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class QuestsRepositoryIT {

  private DataSource dataSource;

  private QuestsRepository questsRepository;
  private QuestRewardsRepository questRewardsRepository;
  private QuestRequirementsRepository questRequirementsRepository;
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

    questsRepository = new QuestsRepository(Logger.getLogger(this.getClass().getName()), dataSource,
        concurrencyHandler);
    questRequirementsRepository = new QuestRequirementsRepository(
        Logger.getLogger(this.getClass().getName()), concurrencyHandler, dataSource);
    questRewardsRepository = new QuestRewardsRepository(Logger.getLogger(this.getClass().getName()),
        dataSource, concurrencyHandler);
    questObjectivesRepository = new QuestObjectivesRepository(
        Logger.getLogger(this.getClass().getName()), dataSource, concurrencyHandler);

    questsRepository.createTable().join();
    questRequirementsRepository.createTable().join();
    questRewardsRepository.createTable().join();
    questObjectivesRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
    questObjectivesRepository.deleteTable().join();
    questRewardsRepository.deleteTable().join();
    questRequirementsRepository.deleteTable().join();
    questsRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a quest table when it does not exist")
  public void createTable_CreatesQuestTableWhenNotExists() {
    Assertions.assertTrue(questsRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the quest when it exists in the database")
  public void findById_ReturnsQuestWhenExists() {

    int id = 1;

    // Models that will be inserted into the database
    QuestModel questModel = new QuestModel(id, "Test Quest", "Test Quest Description", "", "", "",
        0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    QuestRequirementModel questRequirementModel = new QuestRequirementModel(null, id, "Test", "",
        0F);
    QuestRewardModel questRewardModel = new QuestRewardModel(null, id, "Test", "", 0F);
    QuestObjectiveModel questObjectiveModel = new QuestObjectiveModel(null, id, "Test", "", 0F);

    questsRepository.insert(questModel).join();

    int questRequirementId = questRequirementsRepository.insert(questRequirementModel).join();
    int questRewardId = questRewardsRepository.insert(questRewardModel).join();
    int questObjectiveId = questObjectivesRepository.insert(questObjectiveModel).join();

    // Created expected models to compare with the ones returned by the repository
    QuestRequirementModel expectedQuestRequirementModel = new QuestRequirementModel(
        questRequirementId, questRequirementModel.questId(), questRequirementModel.type(),
        questRequirementModel.stringValue(), questRequirementModel.numericValue());

    QuestRewardModel expectedQuestRewardModel = new QuestRewardModel(questRewardId,
        questRewardModel.questId(), questRewardModel.type(), questRewardModel.stringValue(),
        questRewardModel.numericValue());

    QuestObjectiveModel expectedQuestObjectiveModel = new QuestObjectiveModel(questObjectiveId,
        questObjectiveModel.questId(), questObjectiveModel.type(), questObjectiveModel.stringValue(),
        questObjectiveModel.numericValue());

    QuestModel expectedQuestModel = new QuestModel(questModel.id(), questModel.name(),
        questModel.description(), questModel.openingMsg(), questModel.closingMsg(),
        questModel.item(), questModel.timeLimit(), List.of(expectedQuestObjectiveModel),
        List.of(expectedQuestRequirementModel), List.of(expectedQuestRewardModel));

    questsRepository.findById(id).thenAccept(quest -> {
      Assertions.assertTrue(quest.isPresent());
      Assertions.assertEquals(id, quest.get().id());
      Assertions.assertEquals(expectedQuestModel, quest.get());
    }).join();

  }

  @Test
  @DisplayName("findById() returns empty list if no requirements exist")
  public void findById_ReturnsEmptyListNoRequirements() {

    int id = 1;

    // Models that will be inserted into the database
    QuestModel questModel = new QuestModel(id, "Test Quest", "Test Quest Description", "", "", "",
        0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    QuestRewardModel questRewardModel = new QuestRewardModel(null, id, "Test", "", 0F);
    QuestObjectiveModel questObjectiveModel = new QuestObjectiveModel(null, id, "Test", "", 0F);

    questsRepository.insert(questModel).join();

    int questRewardId = questRewardsRepository.insert(questRewardModel).join();
    int questObjectiveId = questObjectivesRepository.insert(questObjectiveModel).join();

    // Created expected models to compare with the ones returned by the repository

    QuestRewardModel expectedQuestRewardModel = new QuestRewardModel(questRewardId,
        questRewardModel.questId(), questRewardModel.type(), questRewardModel.stringValue(),
        questRewardModel.numericValue());

    QuestObjectiveModel expectedQuestObjectiveModel = new QuestObjectiveModel(questObjectiveId,
        questObjectiveModel.questId(), questObjectiveModel.type(), questObjectiveModel.stringValue(),
        questObjectiveModel.numericValue());

    // The expected requirements are an empty list
    QuestModel expectedQuestModel = new QuestModel(questModel.id(), questModel.name(),
        questModel.description(), questModel.openingMsg(), questModel.closingMsg(),
        questModel.item(), questModel.timeLimit(), List.of(expectedQuestObjectiveModel),
        Collections.emptyList(), List.of(expectedQuestRewardModel));

    questsRepository.findById(id).thenAccept(quest -> {
      Assertions.assertTrue(quest.isPresent());
      Assertions.assertEquals(id, quest.get().id());
      Assertions.assertEquals(expectedQuestModel, quest.get());
    }).join();

  }

  @Test
  @DisplayName("findById() returns empty when the quest does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {

    int id = 1;

    questsRepository.findById(id).thenAccept(quest -> Assertions.assertFalse(quest.isPresent()))
        .join();

  }

  @Test
  @DisplayName("insert() successfully inserts a quest into the database")
  public void insert_SuccessfullyInsertsQuestIntoDatabase() {
    int id = 1;

    questsRepository.insert(
            new QuestModel(id, "Test Quest", "Test Quest Description", "", "", "", 0, null, null, null))
        .join();

    questsRepository.findById(id).thenAccept(quest -> Assertions.assertTrue(quest.isPresent()))
        .join();

  }

  @Test
  @DisplayName("delete() successfully deletes a quest from the database")
  public void delete_DeletesQuestFromDatabase() {
    int id = 1;

    // Insert a quest to avoid foreign key errors
    questsRepository.insert(
            new QuestModel(id, "Test Quest", "Test Quest Description", "", "", "", 0, null, null, null))
        .join();

    questsRepository.delete(id).join();

    questsRepository.findById(id).thenAccept(quest -> Assertions.assertFalse(quest.isPresent()))
        .join();

  }

  @Test
  @DisplayName("delete() does not throw when quest does not exist")
  public void delete_DoesNotThrowWhenQuestDoesNotExist() {

    int id = 1;
    Assertions.assertDoesNotThrow(() -> questsRepository.delete(id).join());

  }

  @Test
  @DisplayName("findAll() returns all quests when quests exist in the database")
  public void findAll_ReturnsAllQuestsWhenExists() {
    // Insert some quests into the database
    QuestModel questModel1 = new QuestModel(1, "Test Quest 1", "Test Quest Description 1", "", "", "", 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    QuestModel questModel2 = new QuestModel(2, "Test Quest 2", "Test Quest Description 2", "", "", "", 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    questsRepository.insert(questModel1).join();
    questsRepository.insert(questModel2).join();

    // Call findAll and check that it returns all inserted quests
    questsRepository.findAll().thenAccept(quests -> {
      Assertions.assertEquals(2, quests.size());
      Assertions.assertTrue(quests.contains(questModel1));
      Assertions.assertTrue(quests.contains(questModel2));
    }).join();
  }

  @Test
  @DisplayName("findAll() returns empty set when no quests exist in the database")
  public void findAll_ReturnsEmptySetWhenNoQuestsExist() {
    // Call findAll and check that it returns an empty set
    questsRepository.findAll().thenAccept(quests -> {
      Assertions.assertTrue(quests.isEmpty());
    }).join();
  }

  @Test
  @DisplayName("findAll() returns quests with all related entities when quests exist in the database")
  public void findAll_ReturnsQuestsWithAllRelatedEntitiesWhenExists() {
    // Insert a quest with related entities into the database
    QuestModel questModel = new QuestModel(1, "Test Quest", "Test Quest Description", "", "", "", 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    QuestRequirementModel questRequirementModel = new QuestRequirementModel(null, 1, "Test", "", 0F);
    QuestRewardModel questRewardModel = new QuestRewardModel(null, 1, "Test", "", 0F);
    QuestObjectiveModel questObjectiveModel = new QuestObjectiveModel(null, 1, "Test", "", 0F);

    questsRepository.insert(questModel).join();
    questRequirementsRepository.insert(questRequirementModel).join();
    questRewardsRepository.insert(questRewardModel).join();
    questObjectivesRepository.insert(questObjectiveModel).join();

    // Call findAll and check that it returns the quest with all related entities
    questsRepository.findAll().thenAccept(quests -> {
      Assertions.assertEquals(1, quests.size());
      QuestModel returnedQuest = quests.iterator().next();
      Assertions.assertEquals(1, returnedQuest.requirements().size());
      Assertions.assertEquals(1, returnedQuest.rewards().size());
      Assertions.assertEquals(1, returnedQuest.objectives().size());
    }).join();
  }

}