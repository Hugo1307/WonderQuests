package dev.hugog.minecraft.wonderquests.cache.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hugog.minecraft.wonderquests.config.PluginConfigHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActiveQuestsCacheTest {

  @Mock
  private PluginConfigHandler pluginConfigHandler;

  private ActiveQuestsCache activeQuestsCache;

  private UUID playerId;
  private ActiveQuestDto activeQuestDto;

  @BeforeEach
  void setUp() {
    activeQuestsCache = new ActiveQuestsCache(pluginConfigHandler);
    playerId = UUID.randomUUID();
    activeQuestDto = new ActiveQuestDto(
        playerId,
        1,
        10F,
        2F,
        System.currentTimeMillis(),
        null
    );
  }

  @Test
  @DisplayName("has() returns true when the player has active quests")
  public void has_ReturnsTrueWhenPlayerHasActiveQuests() {

    activeQuestsCache.put(playerId, Set.of(activeQuestDto));
    assertTrue(activeQuestsCache.has(playerId));

  }

  @Test
  @DisplayName("has() returns false when the player does not have active quests")
  public void has_ReturnsFalseWhenPlayerDoesNotHaveActiveQuests() {

    assertFalse(activeQuestsCache.has(playerId));

  }

  @Test
  @DisplayName("get() returns the active quests of the player when they exist")
  public void get_ReturnsActiveQuestsWhenExist() {

    Set<ActiveQuestDto> activeQuests = Set.of(activeQuestDto);
    activeQuestsCache.put(playerId, activeQuests);
    assertEquals(activeQuests, activeQuestsCache.get(playerId));

  }

  @Test
  @DisplayName("get() returns an empty set when the player does not have active quests")
  public void get_ReturnsEmptySetWhenNotExist() {

    assertTrue(activeQuestsCache.get(playerId).isEmpty());

  }

  @Test
  @DisplayName("put() successfully inserts active quests into the cache")
  public void put_InsertsActiveQuestsIntoCache() {

    Set<ActiveQuestDto> activeQuests = Set.of(activeQuestDto);

    activeQuestsCache.put(playerId, activeQuests);

    assertTrue(activeQuestsCache.has(playerId));
    assertEquals(activeQuests, activeQuestsCache.get(playerId));

  }

}