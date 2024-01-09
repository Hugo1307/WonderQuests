package dev.hugog.minecraft.wonderquests.data.connectivity;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.data.repositories.AbstractDataRepository;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class DbInitializer {

  private final Logger logger;

  private final List<AbstractDataRepository<?,?>> dataRepositories;

  @Inject
  public DbInitializer(@Named("bukkitLogger") Logger logger,
      List<AbstractDataRepository<?,?>> dataRepositories) {
    this.logger = logger;
    this.dataRepositories = dataRepositories;
  }

  public CompletableFuture<Void> checkDatabase() {
    return checkTables();
  }

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
        .thenRunAsync(() -> {

          // For each table, check if it does not exist (by checking the corresponding future)
          // If it does not exist, create it
          tablesSortedByPriority.forEach(table -> {
            if (!tableExistFutureList.get(dataRepositories.indexOf(table)).join()) {
              table.createTable().join();
            } else {
              logger.info(String.format("Table '%s' checked. Good to go!", table.getTableName()));
            }
          });

        });

  }

}
