package dev.hugog.minecraft.wonderquests.data.repositories;

import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.models.DataModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import lombok.Getter;

public abstract class AbstractDataRepository<T extends DataModel, C> {

  @Getter
  protected String tableName;

  @Getter
  protected int priority;

  protected Logger logger;
  protected DataSource dataSource;
  protected ConcurrencyHandler concurrencyHandler;

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

  public abstract void createTable();

  public abstract CompletableFuture<Optional<T>> findById(C id);

  public abstract CompletableFuture<C> insert(T model);

  public abstract CompletableFuture<Void> delete(C id);

}
