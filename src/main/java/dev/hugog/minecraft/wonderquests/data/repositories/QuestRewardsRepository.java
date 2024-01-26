package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This class extends the AbstractDataRepository and provides the implementation for the abstract methods.
 * It represents a repository for quest rewards in the game.
 */
public class QuestRewardsRepository extends AbstractDataRepository<QuestRewardModel, Integer> {

  /**
   * Constructor for the QuestRewardsRepository class.
   *
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  @Inject
  public QuestRewardsRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("quest_reward", 1, logger, dataSource, concurrencyHandler);
  }

  /**
   * This method creates the quest_reward table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
  @Override
  public CompletableFuture<Void> createTable() {

    // Create table using SQL
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {
        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest_reward ("
                + "id SERIAL PRIMARY KEY,"
                + "quest_id INT4 REFERENCES quest (id) ON DELETE CASCADE,"
                + "type VARCHAR(31) NOT NULL,"
                + "num_value FLOAT8,"
                + "str_value VARCHAR(255)"
                + ");");

        ps.execute();

        PreparedStatement createIndexPs = con.prepareStatement(
            "CREATE INDEX IF NOT EXISTS quest_reward_quest_id_idx ON quest_reward (quest_id);");

        createIndexPs.execute();

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
   * This method finds a quest reward by its id.
   *
   * @param id the id of the quest reward
   * @return a CompletableFuture that will be completed with an Optional containing the found quest reward, or empty if no quest reward was found.
   */
  @Override
  public CompletableFuture<Optional<QuestRewardModel>> findById(Integer id) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_reward WHERE id = ?;");

        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new QuestRewardModel(
              rs.getInt("id"),
              rs.getInt("quest_id"),
              rs.getString("type"),
              rs.getString("str_value"),
              rs.getFloat("num_value")
          ));
        } else {
          return Optional.empty();
        }

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while finding %s with id %d! Caused by: %s", tableName, id,
                e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method inserts a quest reward into the quest_reward table.
   *
   * @param model the quest reward to insert
   * @return a CompletableFuture that will be completed with the id of the inserted quest reward.
   */
  @Override
  public CompletableFuture<Integer> insert(QuestRewardModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO quest_reward (quest_id, type, num_value, str_value) VALUES (?, ?, ?, ?) RETURNING id;");

        ps.setInt(1, model.questId());
        ps.setString(2, model.type());

        if (model.numericValue() != null) {
          ps.setFloat(3, model.numericValue());
        } else {
          ps.setNull(3, java.sql.Types.FLOAT);
        }

        if (model.stringValue() != null) {
          ps.setString(4, model.stringValue());
        } else {
          ps.setNull(4, java.sql.Types.VARCHAR);
        }

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return rs.getInt("id");
        } else {
          throw new SQLException("No id was returned after inserting a new quest reward!");
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while inserting a new %s! Caused by: %s", tableName,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method deletes a quest reward by its id.
   *
   * @param id the id of the quest reward
   * @return a CompletableFuture that will be completed when the quest reward is deleted.
   */
  @Override
  public CompletableFuture<Void> delete(Integer id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM quest_reward WHERE id = ?;");

        ps.setInt(1, id);
        ps.execute();

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while deleting %s with id %d! Caused by: %s", tableName, id,
                e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method finds all quest rewards by quest id.
   *
   * @param questId the id of the quest
   * @return a CompletableFuture that will be completed with a List containing all found quest rewards.
   */
  public CompletableFuture<List<QuestRewardModel>> findAllByQuestId(Integer questId) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_reward WHERE quest_id = ?;");

        ps.setInt(1, questId);

        ResultSet rs = ps.executeQuery();

        List<QuestRewardModel> objectives = new ArrayList<>();

        while (rs.next()) {
          objectives.add(new QuestRewardModel(
              rs.getInt("id"),
              rs.getInt("quest_id"),
              rs.getString("type"),
              rs.getString("str_value"),
              rs.getFloat("num_value")
          ));
        }

        return objectives;

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while finding objectives for quest with id %d! Caused by: %s",
                questId,
                e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);

  }

}