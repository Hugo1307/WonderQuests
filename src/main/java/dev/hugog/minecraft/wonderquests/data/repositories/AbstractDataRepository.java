package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.DataModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import lombok.Getter;

/**
 * AbstractDataRepository is an abstract class that provides a template for all data repositories.
 * It includes common functionalities such as checking if a table exists, deleting a table, and
 * abstract methods for creating a table, finding a record by id, inserting a record, and deleting a record,
 * which represent common methods for repositories.
 *
 * @param <T> the type of the data model
 * @param <C> the type of the id
 */
public abstract class AbstractDataRepository<T extends DataModel<?>, C> {

  @Getter
  protected final String tableName;

  @Getter
  protected final int priority;

  protected final Logger logger;
  protected final DataSource dataSource;
  protected final ConcurrencyHandler concurrencyHandler;

  /**
   * Constructor for the AbstractDataRepository class.
   *
   * @param tableName          The name of the table this repository is managing.
   * @param priority           The priority of the table, i.e., the order in which it should be
   *                           created. Low priority tables should be created first. Used to avoid
   *                           problems with foreign keys.
   * @param logger             The logger instance used for logging.
   * @param dataSource         The data source instance used for database connectivity.
   * @param concurrencyHandler The concurrency handler instance used for managing concurrency.
   */
  public AbstractDataRepository(String tableName, int priority, Logger logger,
      DataSource dataSource, ConcurrencyHandler concurrencyHandler) {
    this.tableName = tableName;
    this.priority = priority;
    this.logger = logger;
    this.dataSource = dataSource;
    this.concurrencyHandler = concurrencyHandler;
  }

  /**
   * Checks if the table exists in the database.
   *
   * @return a CompletableFuture that will be completed with a boolean indicating whether the table exists.
   */
  public CompletableFuture<Boolean> doesTableExists() {

    return concurrencyHandler.supply(() -> dataSource.execute(con -> {

      try {

        PreparedStatement ps = con.prepareStatement(
            "SELECT EXISTS ("
                + "SELECT FROM information_schema.tables "
                + "WHERE table_schema = 'public' "
                + "AND table_name = ?"
                + ");");
        ps.setString(1, tableName);

        ResultSet rs = ps.executeQuery();
        rs.next();

        return rs.getBoolean(1);

      } catch (SQLException e) {
        logger.severe("Error while checking if the database exists! Caused by: " + e.getMessage());
        return false;
      }

    }), true);

  }

  /**
   * Deletes the table from the database.
   *
   * @return a CompletableFuture that will be completed when the table is deleted.
   */
  public CompletableFuture<Void> deleteTable() {
    return concurrencyHandler.run(() -> dataSource.apply(con -> {

      try {

        Statement statement = con.createStatement();
        // In this context, concatenating the table name is safe as the table name is not user input
        statement.execute(String.format("DROP TABLE IF EXISTS %s CASCADE;", tableName));

      } catch (SQLException e) {
        logger.severe(String.format("Error while deleting the %s table! Caused by: %s", tableName,
            e.getMessage()));
        throw new RuntimeException(e);
      }

    }), true).whenComplete((value, throwable) -> {
      if (throwable == null) {
        logger.warning(String.format("%s table deleted!", tableName));
      }
    });
  }

  /**
   * Creates the table in the database.
   *
   * @return a CompletableFuture that will be completed when the table is created.
   */
  public abstract CompletableFuture<Void> createTable();

  /**
   * Finds a record by its id.
   *
   * @param id the id of the record
   * @return a CompletableFuture that will be completed with an Optional containing the found record, or empty if no record was found.
   */
  public abstract CompletableFuture<Optional<T>> findById(C id);

  /**
   * Inserts a record into the table.
   *
   * @param model the record to insert
   * @return a CompletableFuture that will be completed with the id of the inserted record.
   */
  public abstract CompletableFuture<C> insert(T model);

  /**
   * Deletes a record by its id.
   *
   * @param id the id of the record
   * @return a CompletableFuture that will be completed when the record is deleted.
   */
  public abstract CompletableFuture<Void> delete(C id);

}