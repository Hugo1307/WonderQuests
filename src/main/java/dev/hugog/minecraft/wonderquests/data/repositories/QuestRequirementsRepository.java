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

public class QuestRequirementsRepository extends AbstractDataRepository<QuestRequirementModel, Integer> {

  @Inject
  public QuestRequirementsRepository(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, DataSource dataSource) {
    super("quest_requirement", 1, logger, dataSource, concurrencyHandler);
  }

  @Override
  public CompletableFuture<Void> createTable() {

    // Create quest table using SQL
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement createTablePs = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest_requirement ("
                + "id SERIAL PRIMARY KEY,"
                + "quest_id INT4 REFERENCES quest (id),"
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

  @Override
  public CompletableFuture<Optional<QuestRequirementModel>> findById(Integer id) {
    return null;
  }

  @Override
  public CompletableFuture<Integer> insert(QuestRequirementModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO quest_requirement (quest_id, type, num_value, str_value) VALUES (?, ?, ?, ?) RETURNING id;");

        ps.setInt(1, model.questId());
        ps.setString(2, model.type());
        ps.setFloat(3, model.numericValue());
        ps.setString(4, model.stringValue());

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

  @Override
  public CompletableFuture<Void> delete(Integer id) {
    return null;
  }

  public CompletableFuture<List<QuestRequirementModel>> findAllByQuestId(Integer questId) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest_objective WHERE quest_id = ?;");

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
        logger.severe(String.format("Error while finding objectives for quest with id %d! Caused by: %s", questId,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);

  }

}
