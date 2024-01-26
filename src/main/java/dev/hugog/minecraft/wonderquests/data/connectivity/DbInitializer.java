package dev.hugog.minecraft.wonderquests.data.connectivity;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.data.repositories.AbstractDataRepository;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This class is responsible for initializing and checking the database.
 */
public class DbInitializer {

  private final Logger logger;

  private final List<AbstractDataRepository<?,?>> dataRepositories;

  /**
   * Constructor for the DbInitializer class.
   *
   * @param logger The logger instance used for logging.
   * @param dataRepositories The list of data repositories.
   */
  @Inject
  public DbInitializer(@Named("bukkitLogger") Logger logger,
      List<AbstractDataRepository<?,?>> dataRepositories) {
    this.logger = logger;
    this.dataRepositories = dataRepositories;
  }

  /**
   * This method checks the database.
   *
   * @return a CompletableFuture that will be completed when the database is checked.
   */
  public CompletableFuture<Void> checkDatabase() {
    return checkTables();
  }

  /**
   * This method checks the tables in the database.
   *
   * @return a CompletableFuture that will be completed when the tables are checked.
   */
  private CompletableFuture<Void> checkTables() {

    // List of CompletableFutures, each one of them checks if one of the tables exists
    List<CompletableFuture<Boolean>> tableExistFutureList = dataRepositories
        .stream()
        .map(AbstractDataRepository::doesTableExists)
        .toList();

    // Sort the tables by priority to create them in the correct order
    List<AbstractDataRepository<?,?>> tablesSortedByPriority = dataRepositories.stream()
        .sorted(Comparator.comparingInt(AbstractDataRepository::getPriority))
        .toList();

    // After all tables were checked, we create the ones that do not exist
    // We take the priority into account to avoid creating tables that depend on other tables
    return CompletableFuture.allOf(tableExistFutureList.toArray(CompletableFuture[]::new))
        .thenRun(() -> {

          // For each table, check if it does not exist (by checking the corresponding future)
          // If it does not exist, create it
          tablesSortedByPriority.forEach(table -> {
            if (!tableExistFutureList.get(dataRepositories.indexOf(table)).join()) {
              // We wait for the table to be created before continuing because tables need to be
              // created in a specific order to avoid foreign key constraint errors
              table.createTable().join();
            } else {
              logger.info(String.format("Table '%s' checked. Good to go!", table.getTableName()));
            }
          });

        });

  }

}