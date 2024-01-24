package dev.hugog.minecraft.wonderquests.cache.implementation;

import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.cache.AbstractCache;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class ActiveQuestsCache extends AbstractCache<PlayerQuestKey, ActiveQuestDto> {

  public ActiveQuestsCache() {
    super(Duration.ofSeconds(5));
  }

  public boolean has(UUID playerId) {
    return cache.keySet().stream()
        .anyMatch(playerQuestKey -> playerQuestKey.playerId().equals(playerId));
  }

  public Set<ActiveQuestDto> get(UUID playerId) {
    return cache.keySet().stream()
        .filter(playerQuestKey -> playerQuestKey.playerId().equals(playerId))
        .map(cache::get)
        .collect(Collectors.toSet());
  }

  public void put(UUID playerId, Set<ActiveQuestDto> activeQuests) {
    activeQuests.forEach(
        activeQuestDto -> super.put(new PlayerQuestKey(playerId, activeQuestDto.getQuestId()),
            activeQuestDto));
  }

}
