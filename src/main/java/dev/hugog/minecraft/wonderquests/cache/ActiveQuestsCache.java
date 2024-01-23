package dev.hugog.minecraft.wonderquests.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActiveQuestsCache {

  private final Cache<UUID, Set<ActiveQuestDto>> activeQuestsCache;

  public ActiveQuestsCache() {
    this.activeQuestsCache = CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build();
  }

  public boolean has(UUID key) {
    return activeQuestsCache.getIfPresent(key) != null;
  }

  public Set<ActiveQuestDto> get(UUID uuid) {
    System.out.println("Getting active quests for " + uuid);
    return activeQuestsCache.getIfPresent(uuid);
  }

  public void put(UUID uuid, Set<ActiveQuestDto> activeQuests) {
    activeQuestsCache.put(uuid, activeQuests);
  }

}
