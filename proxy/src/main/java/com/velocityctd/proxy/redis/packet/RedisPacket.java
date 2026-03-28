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

package com.velocityctd.proxy.redis.packet;


import com.velocityctd.proxy.redis.transaction.Transaction;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet that can be sent over redis.
 */
public sealed interface RedisPacket permits AbstractRedisPacket {

  /**
   * Get the unique id of the packet.
   *
   * @return the unique id of the packet
   */
  UUID getId();

  /**
   * Get the type of the packet.
   *
   * @return the type of the packet
   */
  String getType();

  /**
   * Determine if the packet is a one-way packet.
   *
   * @return true if the packet is a one-way packet, false otherwise
   */
  boolean isOneWay();

  /**
   * Determine if the packet is a reply packet.
   *
   * @return true if the packet is a reply packet, false otherwise
   */
  boolean isReply();

  /**
   * Set the packet as a reply packet.
   *
   * @param reply true if the packet is a reply packet, false otherwise
   */
  void setReply(boolean reply);

  /**
   * Get the unique ID of the {@link Transaction} this
   * packet is part of.
   *
   * @return the unique id of the transaction
   */
  @Nullable UUID getTransactionId();

  /**
   * Set the unique id of the {@link Transaction} this
   * packet is part of.
   *
   * @param transactionId the unique id of the transaction
   */
  void setTransactionId(@NotNull UUID transactionId);

  /**
   * Get the type of the {@link Transaction} this packet is part of.
   *
   * @return the type of the transaction
   */
  @Nullable String getTransactionType();

  /**
   * Set the type of the {@link Transaction} this packet is part of.
   *
   * @param transactionType the type of the transaction
   */
  void setTransactionType(@NotNull String transactionType);

  /**
   * Publish the packet to redis.
   */
  void publish();
}
