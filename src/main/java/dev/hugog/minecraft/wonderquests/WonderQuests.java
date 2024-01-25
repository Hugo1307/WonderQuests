package dev.hugog.minecraft.wonderquests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.hugog.minecraft.wonderquests.cache.CacheScheduler;
import dev.hugog.minecraft.wonderquests.commands.BukkitCommandExecutor;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.connectivity.DbInitializer;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.injection.BasicBinderModule;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import dev.hugog.minecraft.wonderquests.listeners.ActiveQuestUpdateListener;
import dev.hugog.minecraft.wonderquests.listeners.GuiClickListener;
import dev.hugog.minecraft.wonderquests.listeners.InteractiveChatListener;
import dev.hugog.minecraft.wonderquests.listeners.PlayerJoinListener;
import dev.hugog.minecraft.wonderquests.listeners.QuestGoalsListener;
import dev.hugog.minecraft.wonderquests.listeners.SignUpdateListener;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.PluginCommand;
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

  @Inject
  private Messaging messaging;

  @Inject
  private BukkitCommandExecutor bukkitCommandExecutor;

  @Inject
  private InteractiveChatListener interactiveChatListener;
  @Inject
  private GuiClickListener guiClickListener;
  @Inject
  private PlayerJoinListener playerJoinListener;

  @Inject
  private QuestGoalsListener questGoalsListener;

  @Inject
  private SignUpdateListener signUpdateListener;

  @Inject
  private ActiveQuestUpdateListener activeQuestUpdateListener;

  @Inject
  private CacheScheduler cacheScheduler;

  @Override
  public void onEnable() {

    initDependencyInjectionModule();

    boolean connectedToDb = dataSource.initDataSource("localhost", "5432", "wonder_quests",
        "postgres", "admin");

    if (!connectedToDb) {
      getLogger().severe("Error while connecting to the database! Disabling plugin...");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    // Check the state of the database. Correct the state if necessary.
    CompletableFuture<Void> databaseCheckFuture = dbInitializer.checkDatabase();
    CompletableFuture<?>[] futuresToWaitFor = concurrencyHandler.getListOfFutures(
        databaseCheckFuture);

    // Wait for the database check to finish before continuing.
    concurrencyHandler.runAfterMultiple(futuresToWaitFor, () -> {

      // Sync Logic

      getLogger().info("Database check finished! Enabling plugin...");

      registerCommands();

      messaging.loadBundles();

      // Register Listener
      getServer().getPluginManager().registerEvents(interactiveChatListener, this);
      getServer().getPluginManager().registerEvents(guiClickListener, this);
      getServer().getPluginManager().registerEvents(playerJoinListener, this);
      getServer().getPluginManager().registerEvents(questGoalsListener, this);
      getServer().getPluginManager().registerEvents(signUpdateListener, this);
      getServer().getPluginManager().registerEvents(activeQuestUpdateListener, this);

      // Start cache scheduler
      cacheScheduler.runTaskTimerAsynchronously(this, 10*20L, 10*20L);

      getLogger().info("Plugin successfully enabled!");

    }, false);

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

  private void registerCommands() {

    PluginCommand command = getCommand("quests");

    if (command != null) {
      command.setExecutor(bukkitCommandExecutor);
    } else {
      getLogger().warning("Default plugin's command not not found!");
    }

  }

}
