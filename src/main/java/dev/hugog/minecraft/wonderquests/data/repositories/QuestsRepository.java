package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class QuestsRepository extends AbstractDataRepository {

  @Inject
  public QuestsRepository(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, DataSource dataSource) {
    super("quest", 0, logger, dataSource, concurrencyHandler);
  }

  @Override
  public void createTable() {

    // Create quest table using SQL
    concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest ("
                + "id SERIAL PRIMARY KEY,"
                + "name VARCHAR(127) NOT NULL,"
                + "description VARCHAR(511),"
                + "opening_msg VARCHAR(255),"
                + "closing_msg VARCHAR(255),"
                + "item VARCHAR(31)"
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

}
