package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class PlayersRepository extends AbstractDataRepository<PlayerModel, UUID> {

  @Inject
  public PlayersRepository(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, DataSource dataSource) {
    super("player", 0, logger, dataSource, concurrencyHandler);
  }

  @Override
  public void createTable() {

    concurrencyHandler.run(() -> dataSource.apply(con -> {

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
    return null;
  }

  @Override
  public CompletableFuture<UUID> insert(PlayerModel model) {
    return null;
  }

  @Override
  public CompletableFuture<Void> delete(UUID id) {
    return null;
  }

}
