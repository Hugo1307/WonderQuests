package dev.hugog.minecraft.wonderquests.config;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This class handles the configuration of the plugin.
 */
public class PluginConfigHandler {

  private final FileConfiguration pluginConfig;

  /**
   * Constructor for the PluginConfigHandler class.
   *
   * @param pluginConfig The configuration of the plugin.
   */
  @Inject
  public PluginConfigHandler(FileConfiguration pluginConfig) {
    this.pluginConfig = pluginConfig;
  }

  /**
   * This method gets the host of the database.
   *
   * @return a String representing the host of the database.
   */
  public String getDatabaseHost() {
    return pluginConfig.getString("Database.Host");
  }

  /**
   * This method gets the port of the database.
   *
   * @return an Integer representing the port of the database.
   */
  public Integer getDatabasePort() {
    return pluginConfig.getInt("Database.Port");
  }

  /**
   * This method gets the name of the database.
   *
   * @return a String representing the name of the database.
   */
  public String getDatabaseName() {
    return pluginConfig.getString("Database.Name");
  }

  /**
   * This method gets the user of the database.
   *
   * @return a String representing the user of the database.
   */
  public String getDatabaseUser() {
    return pluginConfig.getString("Database.User");
  }

  /**
   * This method gets the password of the database.
   *
   * @return a String representing the password of the database.
   */
  public String getDatabasePassword() {
    return pluginConfig.getString("Database.Password");
  }

  /**
   * This method gets the maximum pool size of the database.
   *
   * @return an Integer representing the maximum pool size of the database.
   */
  public Integer getDatabaseMaxPoolSize() {
    return pluginConfig.getInt("Database.Pool.MaxSize");
  }

  /**
   * This method gets the expiration time of the active quests cache.
   *
   * @return an Integer representing the expiration time of the active quests cache.
   */
  public Integer getCacheActiveQuestsExpirationTime() {
    return pluginConfig.getInt("Cache.ActiveQuests.TTL");
  }

}