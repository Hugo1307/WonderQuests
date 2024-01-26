package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
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
 * It represents a repository for quest requirements in the game.
 */
public class QuestRequirementsRepository extends
    AbstractDataRepository<QuestRequirementModel, Integer> {

  /**
   * Constructor for the QuestRequirementsRepository class.
   *
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  @Inject
  public QuestRequirementsRepository(@Named("bukkitLogger") Logger logger,
      DataSource dataSource, ConcurrencyHandler concurrencyHandler) {
    super("quest_requirement", 1, logger, dataSource, concurrencyHandler);
  }

  /**
   * This method creates the quest_requirement table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
  @Override
  public CompletableFuture<Void> createTable() {

    // Create quest table using SQL
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement createTablePs = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest_requirement ("
                + "id SERIAL PRIMARY KEY,"
                + "quest_id INT4 REFERENCES quest (id) ON DELETE CASCADE,"
                + "type VARCHAR(31) NOT NULL,"
                + "num_value FLOAT8,"
                + "str_value VARCHAR(255)"
                + ");");

        createTablePs.execute();

        PreparedStatement createIndexPs = con.prepareStatement(
            "CREATE INDEX IF NOT EXISTS quest_requirement_quest_id_idx ON quest_requirement (quest_id);");

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
   * This method finds a quest requirement by its id.
   *
   * @param id the id of the quest requirement
   * @return a CompletableFuture that will be completed with an Optional containing the found quest requirement, or empty if no quest requirement was found.
   */
  @Override
  public CompletableFuture<Optional<QuestRequirementModel>> findById(Integer id) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_requirement WHERE id = ?;");

        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new QuestRequirementModel(
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
            String.format("Error while finding quest requirement with id %d! Caused by: %s", id,
                e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method inserts a quest requirement into the quest_requirement table.
   *
   * @param model the quest requirement to insert
   * @return a CompletableFuture that will be completed with the id of the inserted quest requirement.
   */
  @Override
  public CompletableFuture<Integer> insert(QuestRequirementModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO quest_requirement (quest_id, type, num_value, str_value) VALUES (?, ?, ?, ?) RETURNING id;");

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
          throw new SQLException("No id was returned after inserting a new quest requirement!");
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while inserting a new %s! Caused by: %s", tableName,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method deletes a quest requirement by its id.
   *
   * @param id the id of the quest requirement
   * @return a CompletableFuture that will be completed when the quest requirement is deleted.
   */
  @Override
  public CompletableFuture<Void> delete(Integer id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM quest_requirement WHERE id = ?;");

        ps.setInt(1, id);

        ps.execute();

      } catch (SQLException e) {
        logger.severe(
            String.format("Error while deleting quest requirement with id %d! Caused by: %s", id,
                e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method finds all quest requirements by quest id.
   *
   * @param questId the id of the quest
   * @return a CompletableFuture that will be completed with a List containing all found quest requirements.
   */
  public CompletableFuture<List<QuestRequirementModel>> findAllByQuestId(Integer questId) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_requirement WHERE quest_id = ?;");

        ps.setInt(1, questId);

        ResultSet rs = ps.executeQuery();

        List<QuestRequirementModel> objectives = new ArrayList<>();

        while (rs.next()) {
          objectives.add(new QuestRequirementModel(
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