package dev.hugog.minecraft.wonderquests.cache;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.cache.implementation.ActiveQuestsCache;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import org.bukkit.scheduler.BukkitRunnable;

public class CacheScheduler extends BukkitRunnable {

  private final ActiveQuestsService activeQuestsService;
  private final ActiveQuestsCache activeQuestsCache;

  @Inject
  public CacheScheduler(ActiveQuestsCache activeQuestsCache,
      ActiveQuestsService activeQuestsService) {

    this.activeQuestsService = activeQuestsService;
    this.activeQuestsCache = activeQuestsCache;

  }

  @Override
  public void run() {

    activeQuestsCache.doForAllExpiredKeys(activeQuestsService::saveActiveQuest);
    activeQuestsCache.invalidateExpired();

  }

}
