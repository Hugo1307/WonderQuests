package dev.hugog.minecraft.wonderquests.cache.implementation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import java.util.concurrent.TimeUnit;

@Singleton
public class QuestsCache {

  private final Cache<Integer, QuestDto> questsCache;

  public QuestsCache() {
    this.questsCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build();
  }

  public boolean has(Integer key) {
    return questsCache.getIfPresent(key) != null;
  }

  public QuestDto get(Integer key) {
    return questsCache.getIfPresent(key);
  }

  public void put(Integer key, QuestDto activeQuests) {
    questsCache.put(key, activeQuests);
  }

}