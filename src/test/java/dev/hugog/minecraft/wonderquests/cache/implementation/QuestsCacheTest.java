package dev.hugog.minecraft.wonderquests.cache.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestsCacheTest {

  private QuestsCache questsCache;
  private QuestDto questDto;

  @BeforeEach
  void setUp() {
    questsCache = new QuestsCache();
    questDto = new QuestDto(
        1,
        "Test Quest",
        "Test Description",
        "Opening Message",
        "Closing Message",
        "sand",
        10,
        null,
        new ArrayList<>(),
        new ArrayList<>()
    );
  }

  @Test
  @DisplayName("has() returns true when the quest is in the cache")
  public void has_ReturnsTrueWhenQuestIsInCache() {
    questsCache.put(1, questDto);
    assertTrue(questsCache.has(1));
  }

  @Test
  @DisplayName("has() returns false when the quest is not in the cache")
  public void has_ReturnsFalseWhenQuestIsNotInCache() {
    assertFalse(questsCache.has(1));
  }

  @Test
  @DisplayName("get() returns the quest when it exists in the cache")
  public void get_ReturnsQuestWhenExistsInCache() {
    questsCache.put(1, questDto);
    assertEquals(questDto, questsCache.get(1));
  }

  @Test
  @DisplayName("get() returns null when the quest does not exist in the cache")
  public void get_ReturnsNullWhenQuestDoesNotExistInCache() {
    assertNull(questsCache.get(1));
  }

  @Test
  @DisplayName("put() successfully inserts a quest into the cache")
  public void put_InsertsQuestIntoCache() {
    questsCache.put(1, questDto);
    assertTrue(questsCache.has(1));
    assertEquals(questDto, questsCache.get(1));
  }

}