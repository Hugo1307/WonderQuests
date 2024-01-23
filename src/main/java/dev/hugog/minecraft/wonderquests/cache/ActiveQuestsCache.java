package dev.hugog.minecraft.wonderquests.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActiveQuestsCache {

  private final Cache<UUID, Set<ActiveQuestDto>> activeQuestsCache;

  @Inject
  public ActiveQuestsCache(QuestsService questsService) {

    // TODO: Change after debug is finished.
    this.activeQuestsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .removalListener(
            (RemovalListener<UUID, Set<ActiveQuestDto>>) notification -> notification.getValue()
                .forEach(questsService::saveActiveQuest))
        .build();

  }

  public boolean has(UUID key) {
    return activeQuestsCache.getIfPresent(key) != null;
  }

  public Set<ActiveQuestDto> get(UUID uuid) {
    return activeQuestsCache.getIfPresent(uuid);
  }

  public void put(UUID uuid, Set<ActiveQuestDto> activeQuests) {
    activeQuestsCache.put(uuid, activeQuests);
  }

  public void invalidate(UUID uuid) {
    activeQuestsCache.invalidate(uuid);
  }

}
