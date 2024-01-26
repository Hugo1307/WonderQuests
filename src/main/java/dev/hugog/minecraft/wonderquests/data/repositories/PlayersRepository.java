package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This class extends the AbstractDataRepository and provides the implementation for the abstract methods.
 * It represents a repository for players in the game.
 */
public class PlayersRepository extends AbstractDataRepository<PlayerModel, UUID> {

  /**
   * Constructor for the PlayersRepository class.
   *
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  @Inject
  public PlayersRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("player", 0, logger, dataSource, concurrencyHandler);
  }

  /**
   * This method creates the player table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
  @Override
  public CompletableFuture<Void> createTable() {

    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS player ("
                + "id UUID PRIMARY KEY"
                + ");");

        ps.execute();

      } catch (SQLException e) {
        logger.severe(String.format("Error while creating the %s table! Caused by: %s", tableName,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true).whenComplete((value, throwable) -> {
      if (throwable == null) {
        logger.info(String.format("%s table created!", tableName));
      }
    });

  }

  /**
   * This method finds a player by its id.
   *
   * @param id the id of the player
   * @return a CompletableFuture that will be completed with an Optional containing the found player, or empty if no player was found.
   */
  @Override
  public CompletableFuture<Optional<PlayerModel>> findById(UUID id) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM player WHERE id = ?;");

        ps.setObject(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new PlayerModel(
              rs.getObject("id", UUID.class)
          ));
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding quest with id %s! Caused by: %s", id,
            e.getMessage()));
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);
  }

  /**
   * This method inserts a player into the player table.
   *
   * @param model the player to insert
   * @return a CompletableFuture that will be completed with the id of the inserted player.
   */
  @Override
  public CompletableFuture<UUID> insert(PlayerModel model) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO player (id) VALUES (?);");

        ps.setObject(1, model.playerId());

        ps.execute();
        return model.playerId();

      } catch (SQLException e) {
        logger.severe(String.format("Error while inserting a new %s! Caused by: %s", tableName,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);

  }

  /**
   * This method deletes a player by its id.
   *
   * @param id the id of the player
   * @return a CompletableFuture that will be completed when the player is deleted.
   */
  @Override
  public CompletableFuture<Void> delete(UUID id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM player WHERE id = ?;");

        ps.setObject(1, id);
        ps.execute();

      } catch (SQLException e) {
        logger.severe(String.format("Error while deleting quest with id %s! Caused by: %s",
            id, e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

}