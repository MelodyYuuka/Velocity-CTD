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

import com.google.common.base.Preconditions;
import com.velocityctd.proxy.redis.packet.RedisPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a handler for a {@link Transaction}.
 *
 * <p>A {@code TransactionHandler} knows how to transform an incoming {@link RedisPacket}
 * into a reply packet of type {@code R}, and is associated with a specific
 * {@link Transaction} class.</p>
 *
 * @param <T> the type of the 'sent' {@link RedisPacket} handled by this transaction
 * @param <R> the type of the reply {@link RedisPacket} produced by this handler
 */
public abstract class TransactionHandler<T extends RedisPacket, R extends RedisPacket> {

  /**
   * The transaction class that this handler is responsible for.
   */
  private final Class<? extends Transaction<T, R>> transactionClass;

  /**
   * Constructs a new {@link TransactionHandler}.
   *
   * @param transactionClass the class of the transaction
   */
  public TransactionHandler(final @NotNull Class<? extends Transaction<T, R>> transactionClass) {
    this.transactionClass = transactionClass;
  }

  /**
   * Handles the given Redis packet and produces a reply packet, if needed.
   *
   * @param packet the incoming packet to handle
   * @return a newly created reply packet, or {@code null} if no reply is required
   */
  public abstract @Nullable R handlePacket(T packet);

  /**
   * Creates the reply packet for the given incoming {@link RedisPacket}, if any.
   *
   * <p>This method delegates to {@link #handlePacket(RedisPacket)} and, if a reply packet
   * is returned, propagates the transaction ID from the incoming packet and marks the
   * reply as a transaction response.</p>
   *
   * @param redisPacket the packet to create the reply packet from
   * @return {@code null} if no reply packet is needed, otherwise the reply packet
   */
  public @Nullable R getReplyPacket(final @NotNull RedisPacket redisPacket) {
    // noinspection unchecked
    final R replyPacket = this.handlePacket((T) redisPacket);
    if (replyPacket == null) {
      return null;
    }

    Preconditions.checkNotNull(redisPacket.getTransactionId());
    replyPacket.setTransactionId(redisPacket.getTransactionId());
    replyPacket.setReply(true);

    return replyPacket;
  }

  /**
   * Gets the class of the {@link Transaction} associated with this handler.
   *
   * @return the transaction class
   */
  public Class<? extends Transaction<T, R>> getTransactionClass() {
    return transactionClass;
  }
}
