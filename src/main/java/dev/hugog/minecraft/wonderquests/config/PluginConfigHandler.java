package dev.hugog.minecraft.wonderquests.config;

import com.google.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfigHandler {

  private final FileConfiguration pluginConfig;

  @Inject
  public PluginConfigHandler(FileConfiguration pluginConfig) {
    this.pluginConfig = pluginConfig;
  }

  public String getDatabaseHost() {
    return pluginConfig.getString("Database.Host");
  }

  public Integer getDatabasePort() {
    return pluginConfig.getInt("Database.Port");
  }

  public String getDatabaseName() {
    return pluginConfig.getString("Database.Name");
  }

  public String getDatabaseUser() {
    return pluginConfig.getString("Database.User");
  }

  public String getDatabasePassword() {
    return pluginConfig.getString("Database.Password");
  }

  public Integer getDatabaseMaxPoolSize() {
    return pluginConfig.getInt("Database.Pool.MaxSize");
  }

  public Integer getCacheActiveQuestsExpirationTime() {
    return pluginConfig.getInt("Cache.ActiveQuests.TTL");
  }

}
