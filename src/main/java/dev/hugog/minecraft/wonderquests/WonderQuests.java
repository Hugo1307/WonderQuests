package dev.hugog.minecraft.wonderquests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.connectivity.DbInitializer;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.injection.BasicBinderModule;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;

public final class WonderQuests extends JavaPlugin {

  @Inject
  private DataSource dataSource;

  @Inject
  private DbInitializer dbInitializer;

  @Inject
  private QuestsService questsService;

  @Inject
  private ConcurrencyHandler concurrencyHandler;

  @Override
  public void onEnable() {

    initDependencyInjectionModule();

    dataSource.initDataSource("localhost", "5432", "wonder_quests", "postgres", "admin");

    // Check the state of the database. Correct the state if necessary.
    CompletableFuture<Void> databaseCheckFuture = dbInitializer.checkDatabase();
    CompletableFuture<?>[] futuresToWaitFor = concurrencyHandler.getListOfFutures(databaseCheckFuture);

    // Sync Logic

    concurrencyHandler.runAfterMultiple(futuresToWaitFor, () -> {

      // Optional<QuestDto> questDto = questsService.getQuestById(22312).join();
      // getLogger().info("Quest: " + questDto);

      getLogger().info("Plugin successfully enabled!");

    }, true);

  }

  @Override
  public void onDisable() {

    dataSource.closeDataSource();

    getLogger().info("Plugin successfully disabled!");

  }

  private void initDependencyInjectionModule() {
    BasicBinderModule guiceBinderModule = new BasicBinderModule(this);
    Injector injector = Guice.createInjector(guiceBinderModule);
    injector.injectMembers(this);
  }

}
