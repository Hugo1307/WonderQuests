package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * ActiveQuestRepository is a class that extends the AbstractDataRepository.
 * It provides the implementation for the abstract methods in the AbstractDataRepository.
 * It also includes additional methods for finding all active quests by player id and saving an active quest.
 */
public class ActiveQuestRepository extends
    AbstractDataRepository<ActiveQuestModel, PlayerQuestKey> {

  /**
   * Constructor for the ActiveQuestRepository class.
   *
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  @Inject
  public ActiveQuestRepository(@Named("bukkitLogger") Logger logger,
      DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("active_quest", 1, logger, dataSource, concurrencyHandler);
  }

  /**
   * Creates the active_quest table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
  @Override
  public CompletableFuture<Void> createTable() {

    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS active_quest ("
                + "player_id UUID REFERENCES player(id) NOT NULL,"
                + "quest_id INTEGER REFERENCES quest(id) NOT NULL,"
                + "target REAL NOT NULL DEFAULT 0,"
                + "progress REAL NOT NULL DEFAULT 0,"
                + "started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
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

  /**
   * Finds an active quest by its id.
   *
   * @param id the id of the active quest
   * @return a CompletableFuture that will be completed with an Optional containing the found active quest, or empty if no active quest was found.
   */
  @Override
  public CompletableFuture<Optional<ActiveQuestModel>> findById(PlayerQuestKey id) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM active_quest JOIN quest ON active_quest.quest_id = quest.id WHERE player_id = ? AND quest_id = ?;");

        ps.setObject(1, id.playerId());
        ps.setInt(2, id.questId());

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

          return Optional.of(new ActiveQuestModel(
              rs.getObject("player_id", UUID.class),
              rs.getInt("quest_id"),
              rs.getFloat("target"),
              rs.getFloat("progress"),
              rs.getTimestamp("started_at").getTime(),
              new QuestModel(
                  rs.getInt("id"),
                  rs.getString("name"),
                  rs.getString("description"),
                  rs.getString("opening_msg"),
                  rs.getString("closing_msg"),
                  rs.getString("item"),
                  rs.getInt("time_limit"),
                  null,
                  Collections.emptySet(),
                  Collections.emptySet()
              )
          ));
        }

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while finding %s with id %s! Caused by: %s", tableName, id,
                e.getMessage()));
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);
  }

  /**
   * Inserts an active quest into the active_quest table.
   *
   * @param model the active quest to insert
   * @return a CompletableFuture that will be completed with the id of the inserted active quest.
   */
  @Override
  public CompletableFuture<PlayerQuestKey> insert(ActiveQuestModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        // The timestamp will be completed with the current timestamp
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO active_quest (player_id, quest_id, target, progress, started_at) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "RETURNING player_id, quest_id;");

        ps.setObject(1, model.playerId());
        ps.setInt(2, model.questId());
        ps.setFloat(3, model.target());
        ps.setFloat(4, model.progress());

        if (model.startedAt() == null) {
          ps.setTimestamp(5, null);
        } else {
          ps.setTimestamp(5, new java.sql.Timestamp(model.startedAt()));
        }

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

  /**
   * Deletes an active quest by its id.
   *
   * @param id the id of the active quest
   * @return a CompletableFuture that will be completed when the active quest is deleted.
   */
  @Override
  public CompletableFuture<Void> delete(PlayerQuestKey id) {

    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM active_quest WHERE player_id = ? AND quest_id = ?;");

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

  /**
   * Finds all active quests by player id.
   *
   * @param playerId the id of the player
   * @return a CompletableFuture that will be completed with a Set containing all found active quests.
   */
  public CompletableFuture<Set<ActiveQuestModel>> findAllByPlayerId(UUID playerId) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM active_quest JOIN quest ON active_quest.quest_id = quest.id WHERE player_id = ?;");

        ps.setObject(1, playerId);

        ResultSet rs = ps.executeQuery();
        Set<ActiveQuestModel> activeQuests = new HashSet<>();

        while (rs.next()) {
          activeQuests.add(new ActiveQuestModel(
              rs.getObject("player_id", UUID.class),
              rs.getInt("quest_id"),
              rs.getFloat("target"),
              rs.getFloat("progress"),
              rs.getTimestamp("started_at").getTime(),
              new QuestModel(
                  rs.getInt("id"),
                  rs.getString("name"),
                  rs.getString("description"),
                  rs.getString("opening_msg"),
                  rs.getString("closing_msg"),
                  rs.getString("item"),
                  rs.getInt("time_limit"),
                  null,
                  Collections.emptySet(),
                  Collections.emptySet()
              )
          ));
        }

        return activeQuests;

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while finding all quests for player %s! Caused by: %s", playerId,
                e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * Saves an active quest.
   *
   * @param model the active quest to save
   * @return a CompletableFuture that will be completed with a boolean indicating whether the active quest was saved successfully.
   */
  public CompletableFuture<Boolean> save(ActiveQuestModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "UPDATE active_quest SET target = ?, progress = ? WHERE player_id = ? AND quest_id = ?;");

        ps.setFloat(1, model.target());
        ps.setFloat(2, model.progress());
        ps.setObject(3, model.playerId());
        ps.setInt(4, model.questId());

        return ps.executeUpdate() > 0;

      } catch (SQLException e) {
        logger.severe(String.format("Error while updating %s with id %s! Caused by: %s", tableName,
            model, e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }


}
