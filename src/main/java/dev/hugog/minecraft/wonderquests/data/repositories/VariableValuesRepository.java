package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class VariableValuesRepository extends AbstractDataRepository {

  @Inject
  public VariableValuesRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("variable_value", 0, logger, dataSource, concurrencyHandler);
  }

  @Override
  public void createTable() {

    // Create quest table using SQL
    concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS variable_value ("
                + "id SERIAL PRIMARY KEY,"
                + "value1 VARCHAR(128),"
                + "value2 REAL"
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
