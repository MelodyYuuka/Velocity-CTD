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

package com.velocityctd.proxy.redis.registration;

import com.velocityctd.proxy.redis.packet.RedisPacket;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a route registration for a specific {@link RedisPacket} type.
 *
 * <p>A {@code RouteRegistration} defines how a particular packet type should be handled
 * once received from Redis. Implementations may wrap consumer-based handlers,
 * transactional handlers, or alternate dispatch strategies.</p>
 *
 * @param <T> the type of {@link RedisPacket} handled by this route registration
 */
public sealed interface RouteRegistration<T extends RedisPacket> permits AbstractRouteRegistration {

  /**
   * Creates a new {@link ConsumerRouteRegistration} for the given {@link RedisPacket}
   * class and consumer handler.
   *
   * @param packetClass the concrete packet class this route handles
   * @param consumer the consumer to invoke when a packet of type {@code T} is received
   * @param <T> the type of the packet handled by the consumer
   * @return a new {@link ConsumerRouteRegistration} instance
   */
  @Contract("_, _ -> new")
  static <T extends RedisPacket> @NotNull ConsumerRouteRegistration<T> consumer(final Class<T> packetClass,
                                                                                final Consumer<T> consumer) {
    return new ConsumerRouteRegistration<>(packetClass, consumer);
  }

  /**
   * Gets the packet class associated with this route registration.
   *
   * @return the {@link Class} object representing the packet type handled by this registration
   */
  Class<T> getPacketClass();
}
