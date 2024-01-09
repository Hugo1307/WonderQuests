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

public class PlayersRepository extends AbstractDataRepository<PlayerModel, UUID> {

  @Inject
  public PlayersRepository(@Named("bukkitLogger") Logger logger,
      DataSource dataSource, ConcurrencyHandler concurrencyHandler) {
    super("player", 0, logger, dataSource, concurrencyHandler);
  }

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

  @Override
  public CompletableFuture<Optional<PlayerModel>> findById(UUID id) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM player WHERE id = ?;");

        ps.setObject(1, UUID.class);
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
