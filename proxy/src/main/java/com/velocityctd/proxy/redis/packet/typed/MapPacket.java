/*
 * Copyright (C) 2018-2026 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocityctd.proxy.redis.packet.typed;

import com.velocityctd.proxy.redis.packet.GenericPacket;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Redis packet whose payload is a {@link Map}.
 *
 * <p>This class implements the {@link Map} interface directly, delegating all operations
 * to the underlying payload supplied to {@link GenericPacket}.</p>
 *
 * @param <K> the type of keys contained in the map
 * @param <V> the type of values contained in the map
 */
public class MapPacket<K, V> extends GenericPacket<Map<K, V>> implements Map<K, V> {

  /**
   * Creates a new {@link MapPacket} with the specified map payload.
   *
   * @param payload the map to wrap inside this packet; must not be null
   */
  public MapPacket(final Map<K, V> payload) {
    super(payload);
  }

  /**
   * Returns the number of key-value mappings in the underlying map.
   *
   * @return the number of entries in this map
   */
  @Override
  public int size() {
    return this.payload.size();
  }

  /**
   * Returns whether the underlying map contains no key-value mappings.
   *
   * @return {@code true} if the map is empty, otherwise {@code false}
   */
  @Override
  public boolean isEmpty() {
    return this.payload.isEmpty();
  }

  /**
   * Checks whether the underlying map contains a mapping for the specified key.
   *
   * @param key the key whose presence is to be tested
   * @return {@code true} if the map contains the key, otherwise {@code false}
   */
  @Override
  public boolean containsKey(final Object key) {
    return this.payload.containsKey(key);
  }

  /**
   * Checks whether the underlying map associates one or more keys with the specified value.
   *
   * @param value the value whose presence is to be tested
   * @return {@code true} if the map contains the value, otherwise {@code false}
   */
  @Override
  public boolean containsValue(final Object value) {
    return this.payload.containsValue(value);
  }

  /**
   * Retrieves the value associated with the specified key.
   *
   * @param key the key whose mapped value is to be returned
   * @return the value mapped to the key, or {@code null} if no mapping exists
   */
  @Override
  public V get(final Object key) {
    return this.payload.get(key);
  }

  /**
   * Associates the specified value with the specified key in the underlying map.
   *
   * @param key the key with which the value is to be associated
   * @param value the value to associate with the key
   * @return the previous value associated with the key, or {@code null} if none existed
   */
  @Override
  public @Nullable V put(final K key, final V value) {
    return this.payload.put(key, value);
  }

  /**
   * Removes the mapping for the specified key from the underlying map, if present.
   *
   * @param key the key whose mapping should be removed
   * @return the value previously associated with the key, or {@code null} if no mapping was found
   */
  @Override
  public V remove(final Object key) {
    return this.payload.remove(key);
  }

  /**
   * Copies all mappings from the specified map into the underlying map.
   *
   * @param m the map from which to copy key-value mappings
   */
  @Override
  public void putAll(final @NotNull Map<? extends K, ? extends V> m) {
    this.payload.putAll(m);
  }

  /**
   * Removes all key-value mappings from the underlying map.
   */
  @Override
  public void clear() {
    this.payload.clear();
  }

  /**
   * Returns a {@link Set} of all keys contained in the underlying map.
   *
   * @return a set of keys
   */
  @Override
  public @NotNull Set<K> keySet() {
    return this.payload.keySet();
  }

  /**
   * Returns a {@link Collection} of all values contained in the underlying map.
   *
   * @return a collection of values
   */
  @Override
  public @NotNull Collection<V> values() {
    return this.payload.values();
  }

  /**
   * Returns a {@link Set} view of all key-value mappings in the underlying map.
   *
   * @return a set of map entries
   */
  @Override
  public @NotNull Set<Entry<K, V>> entrySet() {
    return this.payload.entrySet();
  }
}
