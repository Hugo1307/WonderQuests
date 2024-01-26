package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This class extends the AbstractDataRepository and provides the implementation for the abstract methods.
 * It represents a repository for quest objectives in the game.
 */
public class QuestObjectivesRepository extends
    AbstractDataRepository<QuestObjectiveModel, Integer> {

  /**
   * Constructor for the QuestObjectivesRepository class.
   *
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  @Inject
  public QuestObjectivesRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("quest_objective", 1, logger, dataSource, concurrencyHandler);
  }

  /**
   * This method creates the quest_objective table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
  @Override
  public CompletableFuture<Void> createTable() {

    // Create table using SQL
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement createTablePs = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest_objective ("
                + "id SERIAL PRIMARY KEY,"
                + "quest_id INT4 UNIQUE REFERENCES quest (id) ON DELETE CASCADE,"
                + "type VARCHAR(31) NOT NULL,"
                + "num_value FLOAT8,"
                + "str_value VARCHAR(255)"
                + ");");

        createTablePs.execute();

        PreparedStatement createIndexPs = con.prepareStatement(
            "CREATE INDEX IF NOT EXISTS quest_objective_quest_id_idx ON quest_objective (quest_id);");

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
   * This method finds a quest objective by its id.
   *
   * @param id the id of the quest objective
   * @return a CompletableFuture that will be completed with an Optional containing the found quest objective, or empty if no quest objective was found.
   */
  @Override
  public CompletableFuture<Optional<QuestObjectiveModel>> findById(Integer id) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_objective WHERE id = ?;");

        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new QuestObjectiveModel(
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
   * This method inserts a quest objective into the quest_objective table.
   *
   * @param model the quest objective to insert
   * @return a CompletableFuture that will be completed with the id of the inserted quest objective.
   */
  @Override
  public CompletableFuture<Integer> insert(QuestObjectiveModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO quest_objective (quest_id, type, num_value, str_value) VALUES (?, ?, ?, ?) RETURNING id;");

        ps.setInt(1, model.questId());
        ps.setString(2, model.type());
        ps.setFloat(3, model.numericValue());
        ps.setString(4, model.stringValue());

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return rs.getInt(1);
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
   * This method deletes a quest objective by its id.
   *
   * @param id the id of the quest objective
   * @return a CompletableFuture that will be completed when the quest objective is deleted.
   */
  @Override
  public CompletableFuture<Void> delete(Integer id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM quest_objective WHERE id = ?;");

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
   * This method finds a quest objective by its quest id.
   *
   * @param questId the id of the quest
   * @return a CompletableFuture that will be completed with an Optional containing the found quest objective, or empty if no quest objective was found.
   */
  public CompletableFuture<Optional<QuestObjectiveModel>> findByQuestId(Integer questId) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_objective WHERE quest_id = ?;");

        ps.setInt(1, questId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new QuestObjectiveModel(
              rs.getInt("id"),
              rs.getInt("quest_id"),
              rs.getString("type"),
              rs.getString("str_value"),
              rs.getFloat("num_value")
          ));
        }

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while finding objectives for quest with id %d! Caused by: %s",
                questId,
                e.getMessage()
            )
        );
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);

  }


}