package dev.hugog.minecraft.wonderquests.data.connectivity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import dev.hugog.minecraft.wonderquests.config.PluginConfigHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import lombok.Getter;

/**
 * This class provides a data source for the application using HikariCP.
 */
@Singleton
public class DataSource {

  private HikariDataSource dataSource;

  private final HikariConfig config;

  private final Logger logger;

  @Getter
  private String databaseName;

  @Getter
  private String databaseUser;

  /**
   * Constructor for the DataSource class.
   *
   * @param logger The logger instance used for logging.
   */
  @Inject
  public DataSource(@Named("bukkitLogger") Logger logger) {
    this.config = new HikariConfig();
    this.logger = logger;
  }

  /**
   * This method initializes the data source with the provided parameters.
   *
   * @param host The host of the database.
   * @param port The port of the database.
   * @param databaseName The name of the database.
   * @param username The username for the database.
   * @param password The password for the database.
   * @param maxPoolSize The maximum pool size for the data source.
   * @return a boolean indicating if the data source was initialized successfully.
   */
  public boolean initDataSource(String host, String port, String databaseName, String username,
      String password, int maxPoolSize) {

    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      logger.severe("Error while loading the PostgreSQL driver! Caused by: " + e.getMessage());
    }

    config.setJdbcUrl(
        String.format("jdbc:postgresql://%s:%s/%s?ApplicationName=WonderQuests", host, port,
            databaseName));

    config.setPoolName("WonderQuests-HikariCPPool");
    config.setMaximumPoolSize(maxPoolSize);

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
      return false;
    }

    this.databaseName = databaseName;
    this.databaseUser = username;

    return true;

  }

  /**
   * This method initializes the data source with the parameters from the provided PluginConfigHandler.
   *
   * @param pluginConfigHandler The PluginConfigHandler instance containing the configuration for the data source.
   * @return a boolean indicating if the data source was initialized successfully.
   */
  public boolean initDataSource(PluginConfigHandler pluginConfigHandler) {

    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      logger.severe("Error while loading the PostgreSQL driver! Caused by: " + e.getMessage());
    }

    String host = pluginConfigHandler.getDatabaseHost();
    int port = pluginConfigHandler.getDatabasePort();
    String databaseName = pluginConfigHandler.getDatabaseName();
    String username = pluginConfigHandler.getDatabaseUser();
    String password = pluginConfigHandler.getDatabasePassword();
    int maxPoolSize = pluginConfigHandler.getDatabaseMaxPoolSize();

    return initDataSource(host, String.valueOf(port), databaseName, username, password, maxPoolSize);

  }

  /**
   * This method gets a connection from the data source.
   *
   * @return a Connection instance.
   * @throws SQLException if a database access error occurs.
   */
  private Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * This method applies an operation on a connection.
   *
   * @param operation The operation to be applied on the connection.
   */
  public void apply(Consumer<Connection> operation) {

    try (Connection con = getConnection()) {
      operation.accept(con);
    } catch (SQLException e) {
      logger.severe("Error while applying an operation! Caused by: " + e.getMessage());
      throw new RuntimeException(e);
    }

  }

  /**
   * This method executes an operation on a connection and returns a result.
   *
   * @param operation The operation to be executed on the connection.
   * @return the result of the operation.
   */
  public <T> T execute(Function<Connection, T> operation) {

    try (Connection con = getConnection()) {
      return operation.apply(con);
    } catch (SQLException e) {
      logger.severe("Error while executing an operation! Caused by: " + e.getMessage());
      throw new RuntimeException(e);
    }

  }

  /**
   * This method closes the data source.
   */
  public void closeDataSource() {
    if (dataSource != null) {
      dataSource.close();
    }
  }

}