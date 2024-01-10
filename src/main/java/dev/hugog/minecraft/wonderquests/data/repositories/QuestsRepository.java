package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            "SELECT * FROM quest WHERE id = ?;");

        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(new QuestModel(
              rs.getInt("id"),
              rs.getString("name"),
              rs.getString("description"),
              rs.getString("opening_msg"),
              rs.getString("closing_msg"),
              rs.getString("item"),
              rs.getInt("time_limit")
          ));
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
