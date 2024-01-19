package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class QuestsRepository extends AbstractDataRepository<QuestModel, Integer> {

  @Inject
  public QuestsRepository(@Named("bukkitLogger") Logger logger,
      DataSource dataSource, ConcurrencyHandler concurrencyHandler) {
    super("quest", 0, logger, dataSource, concurrencyHandler);
  }

  @Override
  public CompletableFuture<Void> createTable() {

    // Create quest table using SQL
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest ("
                + "id SERIAL PRIMARY KEY,"
                + "name VARCHAR(127) NOT NULL,"
                + "description VARCHAR(511),"
                + "opening_msg VARCHAR(255),"
                + "closing_msg VARCHAR(255),"
                + "item VARCHAR(31),"
                + "time_limit INTEGER"
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
  public CompletableFuture<Optional<QuestModel>> findById(Integer id) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM quest AS quest " +
                    "LEFT JOIN quest_objective ON quest.id = quest_objective.quest_id " +
                    "LEFT JOIN quest_requirement ON quest.id = quest_requirement.quest_id " +
                    "LEFT JOIN quest_reward ON quest.id = quest_reward.quest_id;");

        // ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        QuestModel questModel = null;

        while (rs.next()) {
          if (questModel == null) {
            questModel = new QuestModel(
                    rs.getInt("quest.id"),
                    rs.getString("quest.name"),
                    rs.getString("quest.description"),
                    rs.getString("quest.opening_msg"),
                    rs.getString("quest.closing_msg"),
                    rs.getString("quest.item"),
                    rs.getInt("quest.time_limit"),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
          } else {
            questModel.objectives().add(new QuestObjectiveModel(
                    rs.getInt("quest_objective.id"),
                    rs.getInt("quest_objective.quest_id"),
                    rs.getString("quest_objective.type"),
                    rs.getString("quest_objective.str_value"),
                    rs.getFloat("quest_objective.num_value")
            ));
            questModel.requirements().add(new QuestRequirementModel(
                    rs.getInt("quest_requirement.id"),
                    rs.getInt("quest_requirement.quest_id"),
                    rs.getString("quest_requirement.type"),
                    rs.getString("quest_requirement.str_value"),
                    rs.getFloat("quest_requirement.num_value")
            ));
            questModel.rewards().add(new QuestRewardModel(
                    rs.getInt("quest_reward.id"),
                    rs.getInt("quest_reward.quest_id"),
                    rs.getString("quest_reward.type"),
                    rs.getString("quest_reward.str_value"),
                    rs.getFloat("quest_reward.num_value")
            ));
          }
        }

        if (questModel != null) {
          return Optional.of(questModel);
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding quest with id %d! Caused by: %s", id,
            e.getMessage()));
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);

  }

  @Override
  public CompletableFuture<Integer> insert(QuestModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO quest (name, description, opening_msg, closing_msg, item) VALUES (?, ?, ?, ?, ?) RETURNING id;");

        ps.setString(1, model.name());
        ps.setString(2, model.description());
        ps.setString(3, model.openingMsg());
        ps.setString(4, model.closingMsg());
        ps.setString(5, model.item());

        ResultSet resultSet = ps.executeQuery();

        if (resultSet.next()) {
          return resultSet.getInt("id");
        } else {
          throw new SQLException("No id returned!");
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while inserting quest with name %s! Caused by: %s",
            model.name(), e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  @Override
  public CompletableFuture<Void> delete(Integer id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM quest WHERE id = ?;");

        ps.setInt(1, id);
        ps.execute();

      } catch (SQLException e) {
        logger.severe(String.format("Error while deleting quest with id %d! Caused by: %s",
            id, e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

}
