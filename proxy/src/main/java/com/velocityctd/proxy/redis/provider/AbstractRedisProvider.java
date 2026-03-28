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

package com.velocityctd.proxy.redis.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.velocityctd.proxy.redis.packet.RedisPacket;
import com.velocityctd.proxy.redis.registration.RouteRegistration;
import com.velocityctd.proxy.redis.transaction.Transaction;
import com.velocityctd.proxy.redis.transaction.TransactionHandler;
import com.velocityctd.proxy.redis.transaction.cache.TransactionCache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an abstract {@link RedisProvider} used to provide a common interface for all redis providers.
 *
 * @see LettuceProvider
 */
public abstract sealed class AbstractRedisProvider implements RedisProvider permits LettuceProvider {

  /**
   * Shared logger for all Redis provider implementations.
   */
  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRedisProvider.class);

  /**
   * Cache of packets that have already been handled recently, used to prevent duplicate
   * processing of the same {@link RedisPacket}. Uses a dummy {@link Byte} value as the cache value.
   */
  protected static final Cache<@NotNull RedisPacket, @NotNull Byte> HANDLED_PACKETS = CacheBuilder.newBuilder() // byte = dummy value
          .expireAfterWrite(10, TimeUnit.SECONDS).build();

  /**
   * Cache of pending {@link Transaction} instances, which are automatically timed out
   * via the associated {@link TransactionCache} callback.
   */
  protected static final TransactionCache PENDING_TRANSACTIONS = new TransactionCache(
          (uuid, transaction) -> transaction.timeout());

  /**
   * Listeners notified whenever the Redis pub/sub connection is re-established after a drop.
   */
  private final List<Runnable> reconnectListeners = new CopyOnWriteArrayList<>();

  /**
   * The registry of all route registrations keyed by packet class name.
   */
  @MonotonicNonNull
  protected final Map<String, RouteRegistration<? extends RedisPacket>> routeRegistrations;

  /**
   * The registry of all transaction handlers keyed by transaction class name.
   */
  @MonotonicNonNull
  protected final Map<String, TransactionHandler<?, ?>> transactionHandlers;

  /**
   * Constructs a new {@link AbstractRedisProvider}.
   */
  public AbstractRedisProvider() {
    this.routeRegistrations = new HashMap<>();
    this.transactionHandlers = new HashMap<>();
  }

  /**
   * Publishes a transaction's sent packet and registers the transaction for timeout handling.
   *
   * @param transaction the transaction whose packet should be published
   * @param timeout the timeout value
   * @param timeUnit the time unit of the timeout
   * @param <T> the type of the Redis packet used in the transaction
   */
  @Override
  public <T extends RedisPacket> void publish(final @NotNull Transaction<T, ?> transaction, final int timeout, final TimeUnit timeUnit) {
    final T sentPacket = transaction.getSentPacket();

    HANDLED_PACKETS.put(sentPacket, (byte) 0);
    PENDING_TRANSACTIONS.put(transaction, timeout, timeUnit);

    this.publish(sentPacket);
  }

  /**
   * Registers a route for a specific {@link RedisPacket} type.
   *
   * @param routeRegistration the route registration to add
   * @param <T> the type of Redis packet handled by the route
   */
  @Override
  public <T extends RedisPacket> void registerRoute(final @NotNull RouteRegistration<T> routeRegistration) {
    final Class<T> packetClass = routeRegistration.getPacketClass();

    if (this.routeRegistrations.containsKey(packetClass.getName())) {
      LOGGER.debug("Route registration for '{}' already exists, overwriting", packetClass.getSimpleName());
    }

    this.routeRegistrations.put(packetClass.getName(), routeRegistration);
  }

  /**
   * Unregisters the route associated with the given packet class, if present.
   *
   * @param packetClass the Redis packet class whose route should be removed
   * @param <T> the type of Redis packet handled by the route
   */
  @Override
  public <T extends RedisPacket> void unregisterRoute(final @NotNull Class<T> packetClass) {
    if (this.routeRegistrations.remove(packetClass.getName()) == null) {
      LOGGER.debug("Route registration for '{}' does not exist, ignoring", packetClass.getSimpleName());
    } else {
      LOGGER.debug("Unregistered route registration for '{}'", packetClass.getSimpleName());
    }
  }

  /**
   * Registers a {@link TransactionHandler} for a given {@link Transaction} type.
   *
   * @param transactionHandler the handler to register
   */
  @Override
  public void registerTransaction(final @NotNull TransactionHandler<?, ?> transactionHandler) {
    final Class<? extends Transaction<?, ?>> transactionClass = transactionHandler.getTransactionClass();

    if (this.transactionHandlers.containsKey(transactionClass.getName())) {
      LOGGER.debug("Transaction handler for '{}' already exists, overwriting", transactionClass.getSimpleName());
    }

    this.transactionHandlers.put(transactionClass.getName(), transactionHandler);
  }

  /**
   * Unregisters the {@link TransactionHandler} associated with the given transaction class, if present.
   *
   * @param transactionClass the transaction class whose handler should be removed
   */
  @Override
  public void unregisterTransaction(final @NotNull Class<? extends Transaction<?, ?>> transactionClass) {
    if (this.transactionHandlers.remove(transactionClass.getName()) == null) {
      LOGGER.debug("Transaction handler for '{}' does not exist, ignoring", transactionClass.getSimpleName());
    } else {
      LOGGER.debug("Unregistered transaction handler for '{}'", transactionClass.getSimpleName());
    }
  }

  /**
   * Gets an immutable list of all registered {@link RouteRegistration} instances.
   *
   * @param <T> the type of Redis packet for which route registrations are requested
   * @return an immutable list of route registrations
   */
  @Override
  public <T extends RedisPacket> @NotNull ImmutableList<@NotNull RouteRegistration<T>> getRouteRegistrations() {
    // noinspection unchecked
    return this.routeRegistrations.values().stream()
            .map(routeRegistration -> (RouteRegistration<T>) routeRegistration)
            .collect(ImmutableList.toImmutableList());
  }

  /**
   * Registers a listener to be called whenever the Redis pub/sub connection is re-established
   * after a disconnection.
   *
   * @param listener the callback to invoke on reconnect
   */
  public void addReconnectListener(final @NotNull Runnable listener) {
    this.reconnectListeners.add(listener);
  }

  /**
   * Invokes all registered reconnect listeners.
   * Called by the provider implementation when the pub/sub channel is re-subscribed.
   */
  protected void fireReconnectListeners() {
    for (Runnable listener : this.reconnectListeners) {
      listener.run();
    }
  }
}
