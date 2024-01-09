package dev.hugog.minecraft.wonderquests.data.connectivity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import lombok.Getter;

@Singleton
public class DataSource {

  private HikariDataSource dataSource;

  private final HikariConfig config;

  private final Logger logger;

  @Getter
  private String databaseName;

  @Getter
  private String databaseUser;

  @Inject
  public DataSource(@Named("bukkitLogger") Logger logger) {
    this.config = new HikariConfig();
    this.logger = logger;
  }

  public void initDataSource(String host, String port, String databaseName, String username,
      String password) {

    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      logger.severe("Error while loading the PostgreSQL driver! Caused by: " + e.getMessage());
    }

    config.setJdbcUrl(
        String.format("jdbc:postgresql://%s:%s/%s?ApplicationName=WonderQuests", host, port,
            databaseName));

    config.setPoolName("WonderQuests-HikariCPPool");
    config.setMaximumPoolSize(5);
    config.setMinimumIdle(1);

    config.setUsername(username);
    config.setPassword(password);

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    try {
      dataSource = new HikariDataSource(config);
    } catch (Exception e) {
      logger.severe("Error while initializing the data source! Caused by: " + e.getMessage());
      if (e instanceof PoolInitializationException) {
        logger.warning("[Hint] Maybe you forgot to create the database?");
      }
    }

    this.databaseName = databaseName;
    this.databaseUser = username;

  }

  private Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public void apply(Consumer<Connection> operation) {

    try (Connection con = getConnection()) {
      operation.accept(con);
    } catch (SQLException e) {
      logger.severe("Error while applying an operation! Caused by: " + e.getMessage());
    }

  }

  public <T> T execute(Function<Connection, T> operation) {

    try (Connection con = getConnection()) {
      return operation.apply(con);
    } catch (SQLException e) {
      logger.severe("Error while executing an operation! Caused by: " + e.getMessage());
    }

    return null;

  }

  public void closeDataSource() {
    if (dataSource != null) {
      dataSource.close();
    }
  }

}