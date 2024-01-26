package dev.hugog.minecraft.wonderquests.data.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.SignModel;
import java.util.Set;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

class SignsRepositoryIT {

  private DataSource dataSource;
  private SignsRepository signsRepository;

  final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1-alpine");

  private SignModel signModel;

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

    signsRepository = new SignsRepository(Logger.getLogger(this.getClass().getName()),
        dataSource,
        concurrencyHandler);

    signsRepository.createTable().join();

    signModel = new SignModel(1, "Test Type", "Test World", 1, 1, 1);

  }

  @AfterEach
  void tearDown() {
    signsRepository.deleteTable().join();
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a signs table when it does not exist")
  public void createTable_CreatesSignsTableWhenNotExists() {
    assertTrue(signsRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the sign when it exists in the database")
  public void findById_ReturnsSignWhenExists() {

    signsRepository.insert(signModel).join();

    signsRepository.findById(1).thenAccept(sign -> {
      assertTrue(sign.isPresent());
      assertEquals(sign.get(), signModel);
    }).join();

  }

  @Test
  @DisplayName("findById() returns empty when the sign does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {

    signsRepository.findById(1)
        .thenAccept(sign -> assertFalse(sign.isPresent())).join();

  }

  @Test
  @DisplayName("insert() successfully inserts a sign into the database")
  public void insert_SuccessfullyInsertsSignIntoDatabase() {

    signsRepository.insert(signModel).join();

    signsRepository.findById(signModel.id())
        .thenAccept(sign -> assertTrue(sign.isPresent())).join();

  }

  @Test
  @DisplayName("delete() successfully deletes a sign from the database")
  public void delete_DeletesSignFromDatabase() {

    signsRepository.insert(signModel).join();

    signsRepository.delete(signModel.id()).join();

    signsRepository.findById(1)
        .thenAccept(sign -> assertFalse(sign.isPresent())).join();

  }

  @Test
  @DisplayName("findAll() returns all signs in the database")
  public void findAll_ReturnsAllSignsInDatabase() {

    SignModel signModel1 = new SignModel(1, "Test Type", "Test World", 1, 1, 1);
    SignModel signModel2 = new SignModel(2, "Test Type", "Test World", 2, 2, 2);

    signsRepository.insert(signModel1).join();
    signsRepository.insert(signModel2).join();

    signsRepository.findAll()
        .thenAccept(signs -> {
          assertEquals(2, signs.size());
          assertTrue(signs.containsAll(Set.of(signModel1, signModel2)));
        }).join();

  }

  @Test
  @DisplayName("deleteByLocation() successfully deletes a sign by location from the database")
  public void deleteByLocation_DeletesSignByLocationFromDatabase() {

    signsRepository.insert(signModel).join();

    signsRepository.deleteByLocation("Test World", 1, 1, 1).join();

    signsRepository.findById(signModel.id())
        .thenAccept(sign -> assertFalse(sign.isPresent())).join();

  }

}