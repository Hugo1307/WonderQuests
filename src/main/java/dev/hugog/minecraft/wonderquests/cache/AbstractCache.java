package dev.hugog.minecraft.wonderquests.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractCache<K, V> implements Cache<K, V> {

  protected final Map<K, V> cache;
  protected final Map<K, Long> writeTimestamps;
  private final Duration expirationTime;

  public AbstractCache(Duration expirationTime) {

    this.expirationTime = expirationTime;

    this.cache = new HashMap<>();
    this.writeTimestamps = new HashMap<>();

  }

  @Override
  public boolean has(K key) {
    return cache.containsKey(key);
  }

  @Override
  public V get(K key) {
    return cache.get(key);
  }

  @Override
  public void put(K key, V value) {
    writeTimestamps.put(key, System.currentTimeMillis());
    cache.put(key, value);
  }

  @Override
  public void invalidate(K key) {
    writeTimestamps.remove(key);
    cache.remove(key);
  }

  @Override
  public void invalidateExpired() {
    keysIterator().forEachRemaining(key -> {
      if (isExpired(key)) {
        invalidate(key);
      }
    });
  }

  public void doForAllExpiredKeys(Consumer<V> doForAllExpiredKeys) {
    keysIterator().forEachRemaining(key -> {
      if (isExpired(key)) {
        doForAllExpiredKeys.accept(get(key));
      }
    });
  }

  @Override
  public boolean isExpired(K key) {
    return writeTimestamps.containsKey(key)
        && System.currentTimeMillis() - writeTimestamps.get(key) > expirationTime.toMillis();
  }

  @Override
  public Iterator<K> keysIterator() {
    return cache.keySet().iterator();
  }

  @Override
  public Iterator<V> valuesIterator() {
    return cache.values().iterator();
  }

}
