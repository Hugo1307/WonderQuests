package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
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

class PlayersRepositoryIT {

  private DataSource dataSource;

  private PlayersRepository playersRepository;

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

    playersRepository = new PlayersRepository(Logger.getLogger(this.getClass().getName()),
        dataSource, concurrencyHandler);

    playersRepository.createTable().join();

  }

  @AfterEach
  void tearDown() {
    dataSource.closeDataSource();
  }

  @Test
  @DisplayName("createTable() successfully creates a table when it does not exist")
  public void createTable_CreatesTable_WhenNotExists() {
    Assertions.assertTrue(playersRepository.doesTableExists().join());
  }

  @Test
  @DisplayName("findById() returns the player when it exists in the database")
  public void findById_ReturnsPlayerWhenExists() {
    UUID id = UUID.randomUUID();

    playersRepository.insert(new PlayerModel(id)).join();

    playersRepository.findById(id)
        .thenAccept(player -> {
          Assertions.assertTrue(player.isPresent());
          Assertions.assertEquals(id, player.get().playerId());
        }).join();
  }

  @Test
  @DisplayName("findById() returns empty when the player does not exist in the database")
  public void findById_ReturnsEmptyWhenNotExists() {
    UUID id = UUID.randomUUID();

    playersRepository.findById(id)
        .thenAccept(player -> Assertions.assertFalse(player.isPresent())).join();
  }

  @Test
  @DisplayName("insert() successfully inserts a player into the database")
  public void insert_InsertsPlayerIntoDatabase() {
    UUID id = UUID.randomUUID();

    playersRepository.insert(new PlayerModel(id)).join();

    playersRepository.findById(id)
        .thenAccept(player -> Assertions.assertTrue(player.isPresent())).join();

  }

  @Test
  @DisplayName("delete() successfully deletes a player from the database")
  public void delete_DeletesPlayer_FromDatabase() {
    UUID id = UUID.randomUUID();

    playersRepository.insert(new PlayerModel(id)).join();
    playersRepository.delete(id).join();

    // Check if the player no longer exists in the database
    playersRepository.findById(id)
        .thenAccept(player -> Assertions.assertFalse(player.isPresent())).join();
  }


}