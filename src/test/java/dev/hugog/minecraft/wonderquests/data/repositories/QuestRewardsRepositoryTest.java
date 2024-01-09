package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
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

class QuestRewardsRepositoryTest {

  private DataSource dataSource;
  private QuestRewardsRepository questRewardsRepository;
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
    questRewardsRepository = new QuestRewardsRepository(Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);

    questsRepository.createTable().join();
    questRewardsRepository.createTable().join();

    // Insert a quest to avoid foreign key errors in further tests
    questsRepository.insert(new QuestModel(1, "Test Quest", "Test Quest Description", "", "", ""))
        .join();

  }

  @AfterEach
  void tearDown() {
    questRewardsRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a quest reward table when it does not exist")
  public void createTable_CreatesQuestRewardTableWhenNotExists() {
    Assertions.assertTrue(questRewardsRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the quest reward when it exists in the database")
  public void findById_ReturnsQuestRewardWhenExists() {
    int id = 1;

    QuestRewardModel questRewardModel = new QuestRewardModel(id, 1, "Test Reward",
        "Test Reward Description", 1.0f);

    questRewardsRepository.insert(questRewardModel).join();

    questRewardsRepository.findById(id).thenAccept(questReward -> {
      Assertions.assertTrue(questReward.isPresent());
      Assertions.assertEquals(id, questReward.get().id());
      Assertions.assertEquals(questReward.get(), questRewardModel);
    }).join();
  }

  @Test
  @DisplayName("findById() returns empty when the quest reward does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {
    int id = 1;

    questRewardsRepository.findById(id)
        .thenAccept(questReward -> Assertions.assertFalse(questReward.isPresent())).join();
  }

  @Test
  @DisplayName("insert() successfully inserts a quest reward into the database")
  public void insert_SuccessfullyInsertsQuestRewardIntoDatabase() {
    int id = 1;

    questRewardsRepository.insert(
        new QuestRewardModel(id, 1, "Test Reward", "Test Reward Description", 1.0f)).join();

    questRewardsRepository.findById(id)
        .thenAccept(questReward -> Assertions.assertTrue(questReward.isPresent())).join();
  }

  @Test
  @DisplayName("delete() successfully deletes a quest reward from the database")
  public void delete_DeletesQuestRewardFromDatabase() {
    int id = 1;

    questRewardsRepository.insert(
        new QuestRewardModel(id, 1, "Test Reward", "Test Reward Description", 1.0f)).join();

    questRewardsRepository.delete(id).join();

    questRewardsRepository.findById(id)
        .thenAccept(questReward -> Assertions.assertFalse(questReward.isPresent())).join();
  }

  @Test
  @DisplayName("delete() does not throw when quest reward does not exist")
  public void delete_DoesNotThrowWhenQuestRewardDoesNotExist() {
    int id = 1;
    Assertions.assertDoesNotThrow(() -> questRewardsRepository.delete(id).join());
  }

  @Test
  @DisplayName("findAllByQuestId() returns all quest rewards for a given quest id")
  public void findAllByQuestId_ReturnsAllQuestRewardsForGivenQuestId() {
    int questId = 1;

    QuestRewardModel questRewardModel1 = new QuestRewardModel(1, questId, "Test Reward 1",
        "Test Reward Description", 1.0f);
    QuestRewardModel questRewardModel2 = new QuestRewardModel(2, questId, "Test Reward 2",
        "Test Reward Description", 1.0f);

    questRewardsRepository.insert(questRewardModel1).join();
    questRewardsRepository.insert(questRewardModel2).join();

    questRewardsRepository.findAllByQuestId(questId)
        .thenAccept(questRequirements -> {
          Assertions.assertEquals(2, questRequirements.size());
          org.assertj.core.api.Assertions.assertThat(questRequirements)
              .containsAll(List.of(questRewardModel1, questRewardModel2));
        }).join();
  }

  @Test
  @DisplayName("findAllByQuestId() returns empty list when no quest rewards exist for a given quest id")
  public void findAllByQuestId_ReturnsEmptyListWhenNoQuestRewardsExistForGivenQuestId() {
    int questId = 1;

    questRewardsRepository.findAllByQuestId(questId)
        .thenAccept(questRewards -> Assertions.assertTrue(questRewards.isEmpty())).join();
  }
}