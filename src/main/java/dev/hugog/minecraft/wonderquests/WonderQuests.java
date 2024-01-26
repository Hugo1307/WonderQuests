package dev.hugog.minecraft.wonderquests;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.hugog.minecraft.wonderquests.cache.CacheScheduler;
import dev.hugog.minecraft.wonderquests.commands.BukkitCommandExecutor;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.config.PluginConfigHandler;
import dev.hugog.minecraft.wonderquests.data.connectivity.DataSource;
import dev.hugog.minecraft.wonderquests.data.connectivity.DbInitializer;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.hooks.EconomyHook;
import dev.hugog.minecraft.wonderquests.injection.BasicBinderModule;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import dev.hugog.minecraft.wonderquests.language.MessagingConfigurator;
import dev.hugog.minecraft.wonderquests.listeners.ActiveQuestUpdateListener;
import dev.hugog.minecraft.wonderquests.listeners.GuiClickListener;
import dev.hugog.minecraft.wonderquests.listeners.InteractiveChatListener;
import dev.hugog.minecraft.wonderquests.listeners.PlayerJoinListener;
import dev.hugog.minecraft.wonderquests.listeners.QuestGoalsListener;
import dev.hugog.minecraft.wonderquests.listeners.SignUpdateListener;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class of the WonderQuests plugin.
 *
 * <p>It handles the enabling and disabling of the plugin, initializes the dependency injection module,
 * saves the default config, configures the language files, connects to the database, registers commands and listeners,
 * starts the cache scheduler, and sets up the Vault Economy Hook.</p>
 */
public final class WonderQuests extends JavaPlugin {

  @Inject private DataSource dataSource;
  @Inject private DbInitializer dbInitializer;
  @Inject private ConcurrencyHandler concurrencyHandler;
  @Inject private Messaging messaging;
  @Inject private BukkitCommandExecutor bukkitCommandExecutor;
  @Inject private InteractiveChatListener interactiveChatListener;
  @Inject private GuiClickListener guiClickListener;
  @Inject private PlayerJoinListener playerJoinListener;
  @Inject private QuestGoalsListener questGoalsListener;
  @Inject private SignUpdateListener signUpdateListener;
  @Inject private ActiveQuestUpdateListener activeQuestUpdateListener;
  @Inject private CacheScheduler cacheScheduler;
  @Inject private MessagingConfigurator messagingConfigurator;
  @Inject private EconomyHook economyHook;

  /**
   * This method is called when the plugin is enabled.
   *
   * <p>It initializes the dependency injection module, saves the default config, configures the language files,
   * connects to the database, checks the state of the database, loads localized messaging bundles,
   * registers commands and listeners, starts the cache scheduler, and sets up the Vault Economy Hook.</p>
   */
  @Override
  public void onEnable() {

    // Initialize the dependency injection module
    initDependencyInjectionModule();

    // Save default config
    saveDefaultConfig();

    // Configure the language files - create new files if needed.
    messagingConfigurator.configure();

    // If we can't connect to the database, we can't continue.
    if (!connectToDatabase()) {
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

      // Load localized messaging bundles
      messaging.loadBundles();

      // Register Commands
      registerCommands();

      // Register Listener
      registerListeners();

      // Start cache scheduler
      cacheScheduler.runTaskTimerAsynchronously(this, 10 * 20L, 10 * 20L);

      // Setup Vault Economy Hook
      economyHook.setupEconomy();

      getLogger().info("Plugin successfully enabled!");

    }, false);

  }

  /**
   * This method is called when the plugin is disabled.
   *
   * <p>It closes the data source.</p>
   */
  @Override
  public void onDisable() {

    dataSource.closeDataSource();
    getLogger().info("Plugin successfully disabled!");

  }

  /**
   * Initializes the dependency injection module.
   */
  private void initDependencyInjectionModule() {
    BasicBinderModule guiceBinderModule = new BasicBinderModule(this);
    Injector injector = Guice.createInjector(guiceBinderModule);
    injector.injectMembers(this);
  }

  /**
   * Registers the plugin's commands.
   */
  private void registerCommands() {

    PluginCommand command = getCommand("quests");

    if (command != null) {
      command.setExecutor(bukkitCommandExecutor);
    } else {
      getLogger().warning("Default plugin's command not not found!");
    }

  }

  /**
   * Registers the plugin's listeners.
   */
  private void registerListeners() {
    getServer().getPluginManager().registerEvents(interactiveChatListener, this);
    getServer().getPluginManager().registerEvents(guiClickListener, this);
    getServer().getPluginManager().registerEvents(playerJoinListener, this);
    getServer().getPluginManager().registerEvents(questGoalsListener, this);
    getServer().getPluginManager().registerEvents(signUpdateListener, this);
    getServer().getPluginManager().registerEvents(activeQuestUpdateListener, this);
  }

  /**
   * Connects to the database.
   * If the connection fails, the plugin is disabled.
   *
   * @return true if the connection was successful, false otherwise.
   */
  private boolean connectToDatabase() {
    boolean connectedToDb = dataSource.initDataSource(new PluginConfigHandler(getConfig()));
    if (!connectedToDb) {
      getLogger().severe("Error while connecting to the database! Disabling plugin...");
      getServer().getPluginManager().disablePlugin(this);
    }
    return connectedToDb;
  }

}
