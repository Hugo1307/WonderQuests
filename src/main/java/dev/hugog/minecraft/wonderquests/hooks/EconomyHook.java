package dev.hugog.minecraft.wonderquests.hooks;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.UUID;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

  private final Logger logger;
  private final Server server;

  private Economy economy;

  @Inject
  public EconomyHook(@Named("bukkitLogger") Logger logger, Server server) {
    this.server = server;
    this.logger = logger;
  }

  public void setupEconomy() {

    RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager()
        .getRegistration(Economy.class);

    if (economyProvider != null) {
      economy = economyProvider.getProvider();
      logger.info("Successfully hooked into Vault economy.");
    } else {
      economy = null;
      logger.warning("Unable to hook into Vault economy. Economy features will be disabled.");
    }

  }

  public void depositPlayer(UUID playerId, double amount) {
    if (checkEconomy()) {
      economy.depositPlayer(getOfflinePlayer(playerId), amount);
    }
  }

  public void withdrawPlayer(UUID playerId, double amount) {
    if (checkEconomy()) {
      economy.withdrawPlayer(getOfflinePlayer(playerId), amount);
    }
  }

  public double getBalance(UUID playerId) {
    if (checkEconomy()) {
      return economy.getBalance(getOfflinePlayer(playerId));
    }
    return 0;
  }

  public boolean has(UUID playerId, double amount) {
    if (checkEconomy()) {
      return economy.has(getOfflinePlayer(playerId), amount);
    }
    return false;
  }

  private boolean checkEconomy() {
    if (economy == null) {
      logger.warning("Unable to perform economy operation - economy not hooked.");
      return false;
    }
    return true;
  }

  private OfflinePlayer getOfflinePlayer(UUID playerId) {
    return server.getOfflinePlayer(playerId);
  }

}
