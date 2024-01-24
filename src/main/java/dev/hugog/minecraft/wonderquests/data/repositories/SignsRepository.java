package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.SignModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class SignsRepository extends AbstractDataRepository<SignModel, Integer> {

  @Inject
  public SignsRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("sign", 0, logger, dataSource, concurrencyHandler);
  }

  @Override
  public CompletableFuture<Void> createTable() {

    // Create quest table using SQL
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS sign (" + "id SERIAL PRIMARY KEY,"
                + "type VARCHAR(64) NOT NULL," + "world_name VARCHAR(128) NOT NULL,"
                + "x INTEGER NOT NULL," + "y INTEGER NOT NULL," + "z INTEGER NOT NULL" + ");");

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
  public CompletableFuture<Optional<SignModel>> findById(Integer id) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement("SELECT * FROM sign WHERE id = ?;");

        ps.setObject(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(
              new SignModel(rs.getInt("id"), rs.getString("type"), rs.getString("world_name"),
                  rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding sign with id %s! Caused by: %s", id,
            e.getMessage()));
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);

  }

  @Override
  public CompletableFuture<Integer> insert(SignModel model) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO sign (type, world_name, x, y, z) VALUES (?, ?, ?, ?, ?) RETURNING id;");

        ps.setString(1, model.type());
        ps.setString(2, model.worldName());
        ps.setInt(3, model.x());
        ps.setInt(4, model.y());
        ps.setInt(5, model.z());

        ResultSet resultSet = ps.executeQuery();

        if (resultSet.next()) {
          return resultSet.getInt("id");
        } else {
          throw new SQLException("No id returned!");
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while inserting sign! Caused by: %s", e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);

  }

  @Override
  public CompletableFuture<Void> delete(Integer id) {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement("DELETE FROM sign WHERE id = ?;");

        ps.setInt(1, id);
        ps.execute();

      } catch (SQLException e) {
        logger.severe(String.format("Error while deleting sign with id %d! Caused by: %s", id,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true);
  }

  public CompletableFuture<Optional<SignModel>> findByLocation(
      String worldName,
      Integer x,
      Integer y,
      Integer z
  ) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT * FROM sign WHERE world_name = ? AND x = ? AND y = ? AND z = ?;");

        ps.setString(1, worldName);
        ps.setInt(2, x);
        ps.setInt(3, y);
        ps.setInt(4, z);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return Optional.of(
              new SignModel(
                  rs.getInt("id"),
                  rs.getString("type"),
                  rs.getString("world_name"),
                  rs.getInt("x"),
                  rs.getInt("y"),
                  rs.getInt("z")
              )
          );
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while finding sign by location %s! Caused by: %s",
            worldName + ":" + x + ":" + y + ":" + z,
            e.getMessage()));
        throw new RuntimeException(e);
      }

      return Optional.empty();

    }), true);

  }

  public CompletableFuture<Integer> deleteByLocation(
      String worldName,
      Integer x,
      Integer y,
      Integer z
  ) {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM sign WHERE world_name = ? AND x = ? AND y = ? AND z = ? RETURNING id;");

        ps.setString(1, worldName);
        ps.setInt(2, x);
        ps.setInt(3, y);
        ps.setInt(4, z);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
          return rs.getInt("id");
        }

      } catch (SQLException e) {
        logger.severe(String.format("Error while deleting sign by location %s! Caused by: %s",
            worldName + ":" + x + ":" + y + ":" + z,
            e.getMessage()));
        throw new RuntimeException(e);
      }

      return null;

    }), true);

  }

}
