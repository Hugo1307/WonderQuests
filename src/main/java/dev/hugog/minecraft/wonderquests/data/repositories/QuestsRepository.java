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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This class extends the AbstractDataRepository and provides the implementation for the abstract methods.
 * It represents a repository for quests in the game.
 */
public class QuestsRepository extends AbstractDataRepository<QuestModel, Integer> {

  /**
   * Constructor for the QuestsRepository class.
   *
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  @Inject
  public QuestsRepository(@Named("bukkitLogger") Logger logger,
      DataSource dataSource, ConcurrencyHandler concurrencyHandler) {
    super("quest", 0, logger, dataSource, concurrencyHandler);
  }

  /**
   * This method creates the quest table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
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

  /**
   * This method finds a quest by its id.
   *
   * @param id the id of the quest
   * @return a CompletableFuture that will be completed with an Optional containing the found quest, or empty if no quest was found.
   */
  @Override
  public CompletableFuture<Optional<QuestModel>> findById(Integer id) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT quest.id AS id, name, description, opening_msg, closing_msg, item, time_limit, "
                + "quest_objective.id AS quest_objective_id, "
                + "quest_objective.type AS quest_objective_type, "
                + "quest_objective.str_value AS quest_objective_str_value, "
                + "quest_objective.num_value AS quest_objective_num_value, "
                + "quest_requirement.id AS quest_requirement_id, "
                + "quest_requirement.type AS quest_requirement_type, "
                + "quest_requirement.str_value AS quest_requirement_str_value, "
                + "quest_requirement.num_value AS quest_requirement_num_value, "
                + "quest_reward.id AS quest_reward_id, "
                + "quest_reward.type AS quest_reward_type, "
                + "quest_reward.str_value AS quest_reward_str_value, "
                + "quest_reward.num_value AS quest_reward_num_value "
                + "FROM quest "
                + "LEFT JOIN quest_objective ON quest.id = quest_objective.quest_id "
                + "LEFT JOIN quest_requirement ON quest.id = quest_requirement.quest_id "
                + "LEFT JOIN quest_reward ON quest.id = quest_reward.quest_id "
                + "WHERE quest.id = ?;");

        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        QuestModel questModel = null;

        while (rs.next()) {
          if (questModel == null) {
            questModel = new QuestModel(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("opening_msg"),
                rs.getString("closing_msg"),
                rs.getString("item"),
                rs.getInt("time_limit"),
                rs.getInt("quest_objective_id") == 0 ? null :
                    new QuestObjectiveModel(
                        rs.getInt("quest_objective_id"),
                        rs.getInt("id"),
                        rs.getString("quest_objective_type"),
                        rs.getString("quest_objective_str_value"),
                        rs.getFloat("quest_objective_num_value")
                    ),
                new HashSet<>(),
                new HashSet<>()
            );
          }

          rs.getInt("quest_requirement_id");

          if (!rs.wasNull()) {
            questModel.requirements().add(new QuestRequirementModel(
                rs.getInt("quest_requirement_id"),
                rs.getInt("id"),
                rs.getString("quest_requirement_type"),
                rs.getString("quest_requirement_str_value"),
                rs.getFloat("quest_requirement_num_value")
            ));
          }

          rs.getInt("quest_reward_id");

          if (!rs.wasNull()) {
            questModel.rewards().add(new QuestRewardModel(
                rs.getInt("quest_reward_id"),
                rs.getInt("id"),
                rs.getString("quest_reward_type"),
                rs.getString("quest_reward_str_value"),
                rs.getFloat("quest_reward_num_value")
            ));
          }

        }

        return Optional.ofNullable(questModel);

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding quest with id %d! Caused by: %s", id,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);

  }

  /**
   * This method inserts a quest into the quest table.
   *
   * @param model the quest to insert
   * @return a CompletableFuture that will be completed with the id of the inserted quest.
   */
  @Override
  public CompletableFuture<Integer> insert(QuestModel model) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO quest (name, description, opening_msg, closing_msg, item, time_limit) VALUES (?, ?, ?, ?, ?, ?) RETURNING id;");

        ps.setString(1, model.name());
        ps.setString(2, model.description());
        ps.setString(3, model.openingMsg());
        ps.setString(4, model.closingMsg());
        ps.setString(5, model.item());
        ps.setInt(6, model.timeLimit());

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

  /**
   * This method deletes a quest by its id.
   *
   * @param id the id of the quest
   * @return a CompletableFuture that will be completed when the quest is deleted.
   */
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

  /**
   * This method finds all quests.
   *
   * @return a CompletableFuture that will be completed with a Set containing all found quests.
   */
  public CompletableFuture<Set<QuestModel>> findAll() {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT quest.id AS id, name, description, opening_msg, closing_msg, item, time_limit, "
                + "quest_objective.id AS quest_objective_id, "
                + "quest_objective.type AS quest_objective_type, "
                + "quest_objective.str_value AS quest_objective_str_value, "
                + "quest_objective.num_value AS quest_objective_num_value, "
                + "quest_requirement.id AS quest_requirement_id, "
                + "quest_requirement.type AS quest_requirement_type, "
                + "quest_requirement.str_value AS quest_requirement_str_value, "
                + "quest_requirement.num_value AS quest_requirement_num_value, "
                + "quest_reward.id AS quest_reward_id, "
                + "quest_reward.type AS quest_reward_type, "
                + "quest_reward.str_value AS quest_reward_str_value, "
                + "quest_reward.num_value AS quest_reward_num_value "
                + "FROM quest "
                + "LEFT JOIN quest_objective ON quest.id = quest_objective.quest_id "
                + "LEFT JOIN quest_requirement ON quest.id = quest_requirement.quest_id "
                + "LEFT JOIN quest_reward ON quest.id = quest_reward.quest_id;");

        ResultSet rs = ps.executeQuery();
        Set<QuestModel> questModels = new HashSet<>();

        while (rs.next()) {

          int questId = rs.getInt("id");

          Optional<QuestModel> questModelOptional = questModels.stream()
              .filter(questModel -> questModel.id() == questId)
              .findFirst();

          if (questModelOptional.isEmpty()) {
            questModels.add(new QuestModel(
                questId,
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("opening_msg"),
                rs.getString("closing_msg"),
                rs.getString("item"),
                rs.getInt("time_limit"),
                rs.getInt("quest_objective_id") == 0 ? null :
                    new QuestObjectiveModel(
                        rs.getInt("quest_objective_id"),
                        rs.getInt("id"),
                        rs.getString("quest_objective_type"),
                        rs.getString("quest_objective_str_value"),
                        rs.getFloat("quest_objective_num_value")
                    ),
                new HashSet<>(),
                new HashSet<>()
            ));
            questModelOptional = questModels.stream()
                .filter(questModel -> questModel.id() == questId)
                .findFirst();
          }

          if (questModelOptional.isEmpty()) {
            continue;
          }

          QuestModel ownerQuestModel = questModelOptional.get();

          rs.getInt("quest_requirement_id");

          if (!rs.wasNull()) {
            ownerQuestModel.requirements().add(new QuestRequirementModel(
                rs.getInt("quest_requirement_id"),
                rs.getInt("id"),
                rs.getString("quest_requirement_type"),
                rs.getString("quest_requirement_str_value"),
                rs.getFloat("quest_requirement_num_value")
            ));
          }

          rs.getInt("quest_reward_id");

          if (!rs.wasNull()) {
            ownerQuestModel.rewards().add(new QuestRewardModel(
                rs.getInt("quest_reward_id"),
                rs.getInt("id"),
                rs.getString("quest_reward_type"),
                rs.getString("quest_reward_str_value"),
                rs.getFloat("quest_reward_num_value")
            ));
          }

        }

        return questModels;

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding all quests! Caused by: %s",
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  /**
   * This method finds all quests in a given interval.
   *
   * @param bottom the lower bound of the interval
   * @param top the upper bound of the interval
   * @return a CompletableFuture that will be completed with a List containing all found quests in the given interval.
   */
  public CompletableFuture<List<QuestModel>> findAllInInterval(Integer bottom, Integer top) {
    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT quest.id AS id, name, description, opening_msg, closing_msg, item, time_limit, "
                + "quest_objective.id AS quest_objective_id, "
                + "quest_objective.type AS quest_objective_type, "
                + "quest_objective.str_value AS quest_objective_str_value, "
                + "quest_objective.num_value AS quest_objective_num_value, "
                + "quest_requirement.id AS quest_requirement_id, "
                + "quest_requirement.type AS quest_requirement_type, "
                + "quest_requirement.str_value AS quest_requirement_str_value, "
                + "quest_requirement.num_value AS quest_requirement_num_value, "
                + "quest_reward.id AS quest_reward_id, "
                + "quest_reward.type AS quest_reward_type, "
                + "quest_reward.str_value AS quest_reward_str_value, "
                + "quest_reward.num_value AS quest_reward_num_value "
                + "FROM quest "
                + "LEFT JOIN quest_objective ON quest.id = quest_objective.quest_id "
                + "LEFT JOIN quest_requirement ON quest.id = quest_requirement.quest_id "
                + "LEFT JOIN quest_reward ON quest.id = quest_reward.quest_id "
                + "ORDER BY quest.id "
                + "LIMIT ? OFFSET ?;");

        ps.setInt(1, top);
        ps.setInt(2, bottom);

        ResultSet rs = ps.executeQuery();
        List<QuestModel> questModels = new ArrayList<>();

        while (rs.next()) {

          int questId = rs.getInt("id");

          Optional<QuestModel> questModelOptional = questModels.stream()
              .filter(questModel -> questModel.id() == questId)
              .findFirst();

          if (questModelOptional.isEmpty()) {
            questModels.add(new QuestModel(
                questId,
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("opening_msg"),
                rs.getString("closing_msg"),
                rs.getString("item"),
                rs.getInt("time_limit"),
                rs.getInt("quest_objective_id") == 0 ? null :
                    new QuestObjectiveModel(
                        rs.getInt("quest_objective_id"),
                        rs.getInt("id"),
                        rs.getString("quest_objective_type"),
                        rs.getString("quest_objective_str_value"),
                        rs.getFloat("quest_objective_num_value")
                    ),
                new HashSet<>(),
                new HashSet<>()
            ));
            questModelOptional = questModels.stream()
                .filter(questModel -> questModel.id() == questId)
                .findFirst();
          }

          if (questModelOptional.isEmpty()) {
            continue;
          }

          QuestModel ownerQuestModel = questModelOptional.get();

          rs.getInt("quest_requirement_id");

          if (!rs.wasNull()) {
            ownerQuestModel.requirements().add(new QuestRequirementModel(
                rs.getInt("quest_requirement_id"),
                rs.getInt("id"),
                rs.getString("quest_requirement_type"),
                rs.getString("quest_requirement_str_value"),
                rs.getFloat("quest_requirement_num_value")
            ));
          }

          rs.getInt("quest_reward_id");

          if (!rs.wasNull()) {
            ownerQuestModel.rewards().add(new QuestRewardModel(
                rs.getInt("quest_reward_id"),
                rs.getInt("id"),
                rs.getString("quest_reward_type"),
                rs.getString("quest_reward_str_value"),
                rs.getFloat("quest_reward_num_value")
            ));
          }

        }

        return questModels;

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding all quests! Caused by: %s",
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

}
