package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.CompletedQuestModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class CompletedQuestRepository extends AbstractDataRepository<CompletedQuestModel, PlayerQuestKey> {

  @Inject
  public CompletedQuestRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("completed_quest", 1, logger, dataSource, concurrencyHandler);
  }

  @Override
  public CompletableFuture<Void> createTable() {

    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS completed_quest ("
                + "player_id UUID REFERENCES player(id) NOT NULL,"
                + "quest_id INTEGER REFERENCES quest(id) NOT NULL,"
                + "PRIMARY KEY (player_id, quest_id)"
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
  public CompletableFuture<Optional<CompletedQuestModel>> findById(PlayerQuestKey id) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM completed_quest WHERE player_id = ? AND quest_id = ?;");

        ps.setObject(1, id.playerId());
        ps.setInt(2, id.questId());

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new CompletedQuestModel(
              rs.getObject("player_id", UUID.class),
              rs.getInt("quest_id")
          ));
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding %s with id %s! Caused by: %s", tableName, id,
            e.getMessage()));
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);

  }

  @Override
  public CompletableFuture<PlayerQuestKey> insert(CompletedQuestModel model) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO completed_quest (player_id, quest_id) VALUES (?, ?);");

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          UUID playerId = rs.getObject("player_id", UUID.class);
          Integer questId = rs.getInt("quest_id");
          return new PlayerQuestKey(playerId, questId);
        } else {
          throw new SQLException("No keys generated!");
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while inserting a new %s! Caused by: %s", tableName,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);

  }

  @Override
  public CompletableFuture<Void> delete(PlayerQuestKey id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM quest WHERE player_id = ? AND quest_id = ?;");

        ps.setObject(1, id.playerId());
        ps.setInt(2, id.questId());
        ps.execute();

      } catch (SQLException e) {
        logger.severe(String.format("Error while deleting %s with id %s! Caused by: %s",
            tableName, id, e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

}
