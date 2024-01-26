package dev.hugog.minecraft.wonderquests.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbstractCacheTest {

  private AbstractCache<String, Integer> abstractCache;

  @BeforeEach
  void setUp() {
    abstractCache = new AbstractCache<>(Duration.ofMillis(200)) {};
  }

  @Test
  @DisplayName("put() successfully inserts a value into the cache")
  public void put_InsertsValueIntoCache() {
    abstractCache.put("TestKey", 1);
    assertTrue(abstractCache.has("TestKey"));
    assertEquals(1, abstractCache.get("TestKey"));
  }

  @Test
  @DisplayName("get() returns the value when it exists in the cache")
  public void get_ReturnsValueWhenExists() {
    abstractCache.put("TestKey", 1);
    assertEquals(1, abstractCache.get("TestKey"));
  }

  @Test
  @DisplayName("get() returns null when the value does not exist in the cache")
  public void get_ReturnsNullWhenNotExists() {
    assertNull(abstractCache.get("TestKey"));
  }

  @Test
  @DisplayName("invalidate() successfully removes a value from the cache")
  public void invalidate_RemovesValueFromCache() {
    abstractCache.put("TestKey", 1);
    abstractCache.invalidate("TestKey");
    assertFalse(abstractCache.has("TestKey"));
  }

  @Test
  @DisplayName("invalidateExpired() successfully removes expired values from the cache")
  public void invalidateExpired_RemovesExpiredValuesFromCache() throws InterruptedException {
    abstractCache.put("TestKey", 1);
    Thread.sleep(400);
    abstractCache.invalidateExpired();
    assertFalse(abstractCache.has("TestKey"));
  }

  @Test
  @DisplayName("isExpired() returns true when the value is expired")
  public void isExpired_ReturnsTrueWhenExpired() throws InterruptedException {
    abstractCache.put("TestKey", 1);
    Thread.sleep(400);
    assertTrue(abstractCache.isExpired("TestKey"));
  }

  @Test
  @DisplayName("isExpired() returns false when the value is not expired")
  public void isExpired_ReturnsFalseWhenNotExpired() {
    abstractCache.put("TestKey", 1);
    assertFalse(abstractCache.isExpired("TestKey"));
  }

  @Test
  @DisplayName("doForAllExpiredKeys() performs the action for all expired keys")
  public void doForAllExpiredKeys_PerformsActionForAllExpiredKeys() throws InterruptedException {

    List<Integer> expiredValues = new ArrayList<>();

    abstractCache.put("TestKey1", 1);
    abstractCache.put("TestKey2", 2);
    Thread.sleep(400);

    Consumer<Integer> action = expiredValues::add;
    abstractCache.doForAllExpiredKeys(action);

    assertTrue(expiredValues.contains(1));
    assertTrue(expiredValues.contains(2));
  }

  @Test
  @DisplayName("doForAllExpiredKeys() does not perform the action for non-expired keys")
  public void doForAllExpiredKeys_DoesNotPerformActionForNonExpiredKeys() {

    List<Integer> expiredValues = new ArrayList<>();

    abstractCache.put("TestKey1", 1);
    abstractCache.put("TestKey2", 2);

    Consumer<Integer> action = expiredValues::add;
    abstractCache.doForAllExpiredKeys(action);

    assertFalse(expiredValues.contains(1));
    assertFalse(expiredValues.contains(2));
  }

}