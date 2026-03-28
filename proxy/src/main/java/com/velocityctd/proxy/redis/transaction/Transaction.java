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

package com.velocityctd.proxy.redis.transaction;

import com.velocityctd.proxy.redis.VelocityRedis;
import com.velocityctd.proxy.redis.packet.RedisPacket;
import com.velocityctd.proxy.redis.provider.RedisProvider;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a transaction process that has a {@link T sent-packet} and a {@link R reply-packet}. The transaction's
 * behaviour can be configured using the {@link Transaction#onTimeout(Consumer)} and {@link Transaction#onComplete(Consumer)}.
 *
 * @param <T> the type of the sent {@link RedisPacket}
 * @param <R> the type of the expected reply {@link RedisPacket}
 */
public class Transaction<T extends RedisPacket, R extends RedisPacket> {

  /**
   * The default timeout value (in seconds) used for transactions.
   */
  public static final int DEFAULT_TIMEOUT = 5;

  /**
   * The default time unit associated with {@link #DEFAULT_TIMEOUT}.
   */
  public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

  /**
   * Shared logger for transaction-related debug and warning messages.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

  /**
   * Unique identifier assigned to this transaction.
   */
  private final UUID transactionId;

  /**
   * The packet sent as part of this transaction.
   */
  private final T sentPacket;

  /**
   * The timeout duration to use for this transaction, expressed in {@link #timeUnit}.
   */
  private int timeout = 5;

  /**
   * The time unit associated with {@link #timeout}.
   */
  private TimeUnit timeUnit = TimeUnit.SECONDS;

  /**
   * Consumer invoked when the transaction times out.
   */
  private Consumer<T> timeoutConsumer;

  /**
   * Consumer invoked when a reply packet is received for this transaction.
   */
  private Consumer<R> completeConsumer;

  /**
   * Constructs a new {@link Transaction} given an instance of the required sent-packet.
   *
   * @param sentPacket the sent-packet instance to publish
   */
  public Transaction(final @NotNull T sentPacket) {
    this.transactionId = UUID.randomUUID();
    this.sentPacket = sentPacket;
    this.sentPacket.setTransactionId(this.transactionId);
    this.sentPacket.setTransactionType(this.getClass().getName());
  }

  /**
   * Publish the {@link Transaction} to all subscribers on the redis.
   *
   * @param provider the {@link RedisProvider} to publish the transaction to
   * @return itself for chaining
   *
   * @see RedisProvider#publish(Transaction, int, TimeUnit)
   */
  public Transaction<T, R> publish(final @NotNull RedisProvider provider) {
    provider.publish(this);
    return this;
  }

  /**
   * Publish the {@link Transaction} to all subscribers on the redis using the global
   * {@link VelocityRedis} provider instance.
   *
   * @return itself for chaining
   *
   * @throws IllegalStateException if no Redis provider is available
   * @see RedisProvider#publish(Transaction, int, TimeUnit)
   */
  public Transaction<T, R> publish() {
    final RedisProvider provider = VelocityRedis.INSTANCE.getProvider();
    if (provider == null) {
      throw new IllegalStateException("No redis instance has been provided");
    }

    return publish(provider);
  }

  /**
   * Set the timeout for the {@link Transaction}.
   *
   * @param timeout  the timeout in the given time unit
   * @param timeUnit the time unit of the timeout argument
   * @return itself for chaining
   */
  public Transaction<T, R> setTimeout(final int timeout, final TimeUnit timeUnit) {
    this.timeout = timeout;
    this.timeUnit = timeUnit;
    return this;
  }

  /**
   * Set the timeout consumer for the {@link Transaction}.
   *
   * @param timeoutConsumer the consumer to call when the transaction times out
   * @return itself for chaining
   */
  public Transaction<T, R> onTimeout(final Consumer<T> timeoutConsumer) {
    this.timeoutConsumer = timeoutConsumer;
    return this;
  }

  /**
   * Set the reply consumer for the {@link Transaction}.
   *
   * @param completeConsumer the consumer to call when the transaction receives a reply
   * @return itself for chaining
   */
  public Transaction<T, R> onComplete(final Consumer<R> completeConsumer) {
    this.completeConsumer = completeConsumer;
    return this;
  }

  /**
   * Complete the {@link Transaction} by accepting the {@link Transaction#onComplete(Consumer)} consumer.
   *
   * @param replyPacket the reply packet to use as acceptance
   */
  @SuppressWarnings("unchecked")
  public void complete(final RedisPacket replyPacket) {
    if (this.completeConsumer != null) {
      try {
        this.completeConsumer.accept((R) replyPacket);
      } catch (ClassCastException ignored) {
        LOGGER.warn("Reply packet contains invalid type: {}", replyPacket.getClass());
      }
    } else {
      this.timeout();
    }
  }

  /**
   * Timeout the {@link Transaction} by accepting the {@link Transaction#onTimeout(Consumer)}.
   *
   * @apiNote This method is called automatically when the transaction times out, which
   *          can be configured using {@link Transaction#setTimeout(int, TimeUnit)}
   */
  public void timeout() {
    if (this.timeoutConsumer != null) {
      this.timeoutConsumer.accept(this.sentPacket);
    }
  }

  /**
   * Get the unique id of the {@link Transaction}.
   *
   * @return the unique id of the transaction
   */
  public UUID getTransactionId() {
    return transactionId;
  }

  /**
   * Get the sent-packet of the {@link Transaction}.
   *
   * @return the sent-packet of the transaction
   */
  public T getSentPacket() {
    return sentPacket;
  }

  /**
   * Get the timeout of the {@link Transaction}.
   *
   * @return the timeout of the transaction
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Get the time unit of the {@link Transaction}.
   *
   * @return the time unit of the transaction
   */
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }
}
