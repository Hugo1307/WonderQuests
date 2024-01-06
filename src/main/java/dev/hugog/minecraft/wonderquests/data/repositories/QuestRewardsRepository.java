package dev.hugog.minecraft.wonderquests.data.repositories;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class QuestRewardsRepository extends AbstractDataRepository {

  @Inject
  public QuestRewardsRepository(@Named("bukkitLogger") Logger logger, DataSource dataSource,
      ConcurrencyHandler concurrencyHandler) {
    super("quest_reward", 1, logger, dataSource, concurrencyHandler);
  }

  @Override
  public void createTable() {

    // Create table using SQL
    concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "CREATE TABLE IF NOT EXISTS quest_reward ("
                + "id UUID PRIMARY KEY,"
                + "quest_id INT4 REFERENCES quest (id),"
                + "type VARCHAR(31) NOT NULL,"
                + "value JSON NOT NULL"
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
