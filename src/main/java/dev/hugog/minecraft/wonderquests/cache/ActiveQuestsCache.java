package dev.hugog.minecraft.wonderquests.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

@Singleton
public class ActiveQuestsCache {

  private final Cache<UUID, Set<ActiveQuestDto>> activeQuestsCache;

  @Getter
  private final List<Set<ActiveQuestDto>> activeQuestsToSave = new ArrayList<>();

  public ActiveQuestsCache() {

    // TODO: Change after debug is finished.
    this.activeQuestsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .removalListener(
            (RemovalListener<UUID, Set<ActiveQuestDto>>) notification -> {
              activeQuestsToSave.add(notification.getValue());
              System.out.println("Evicted");
            })
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

  public void clearSaved() {
    activeQuestsToSave.clear();
  }

}
