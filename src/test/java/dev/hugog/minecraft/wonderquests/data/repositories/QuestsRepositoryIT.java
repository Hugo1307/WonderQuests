package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.logging.Logger;
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

    questsRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
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

    QuestModel questModel = new QuestModel(id, "Test Quest", "Test Quest Description", "", "", "");

    questsRepository.insert(questModel)
        .join();

    questsRepository.findById(id).thenAccept(quest -> {
      Assertions.assertTrue(quest.isPresent());
      Assertions.assertEquals(id, quest.get().id());
      Assertions.assertEquals(quest.get(), questModel);
    }).join();

  }

  @Test
  @DisplayName("findById() returns empty when the quest does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {

    int id = 1;

    questsRepository.findById(id)
        .thenAccept(quest -> Assertions.assertFalse(quest.isPresent()))
        .join();

  }

  @Test
  @DisplayName("insert() successfully inserts a quest into the database")
  public void insert_SuccessfullyInsertsQuestIntoDatabase() {
    int id = 1;

    questsRepository.insert(new QuestModel(id, "Test Quest", "Test Quest Description", "", "", ""))
        .join();

    questsRepository.findById(id)
        .thenAccept(quest -> Assertions.assertTrue(quest.isPresent()))
        .join();

  }

  @Test
  @DisplayName("delete() successfully deletes a quest from the database")
  public void delete_DeletesQuestFromDatabase() {
    int id = 1;

    // Insert a quest to avoid foreign key errors
    questsRepository.insert(new QuestModel(id, "Test Quest", "Test Quest Description", "", "", ""))
        .join();

    questsRepository.delete(id).join();

    questsRepository.findById(id)
        .thenAccept(quest -> Assertions.assertFalse(quest.isPresent()))
        .join();

  }

  @Test
  @DisplayName("delete() does not throw when quest does not exist")
  public void delete_DoesNotThrowWhenQuestDoesNotExist() {

    int id = 1;
    Assertions.assertDoesNotThrow(() -> questsRepository.delete(id).join());

  }

}