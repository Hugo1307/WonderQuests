package dev.hugog.minecraft.wonderquests.cache;

import java.util.Iterator;

public interface Cache<K, V> {

  boolean has(K key);

  V get(K key);

  void put(K key, V value);

  void invalidate(K key);

  void invalidateExpired();

  boolean isExpired(K key);

  Iterator<K> keysIterator();

  Iterator<V> valuesIterator();

}
