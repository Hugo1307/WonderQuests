package dev.hugog.minecraft.wonderquests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.connectivity.DbInitializer;
import dev.hugog.minecraft.wonderquests.injection.BasicBinderModule;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;

public final class WonderQuests extends JavaPlugin {

  @Inject
  private DataSource dataSource;

  @Inject
  private DbInitializer dbInitializer;

  @Override
  public void onEnable() {

    initDependencyInjectionModule();

    dataSource.initDataSource("localhost", "5432", "wonder_quests",
        "postgres", "admin");

    // Check the state of the database. Correct the state if necessary.
    CompletableFuture<Void> databaseCheckFuture = dbInitializer.checkDatabase();

    CompletableFuture.allOf(databaseCheckFuture).whenComplete((value, throwable) -> {
      if (throwable == null) {
        getLogger().info("Plugin successfully enabled!");
      }
    });

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
