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

/**
 * Represents a {@link RouteRegistration} for a specific {@link RedisPacket} type
 * that is handled via a {@link Consumer}.
 *
 * <p>This registration type is used for one-way packets, where no reply is required
 * and processing is performed directly by the provided consumer.</p>
 *
 * @param <T> the type of {@link RedisPacket} handled by this registration
 */
public non-sealed class ConsumerRouteRegistration<T extends RedisPacket> extends AbstractRouteRegistration<T> {

  /**
   * The {@link Consumer} that processes packets of type {@code T}.
   */
  private final Consumer<T> consumer;

  /**
   * Constructs a new {@link ConsumerRouteRegistration}.
   *
   * @param packetClass the packet class this registration handles
   * @param consumer the consumer that will process incoming packets of type {@code T}
   */
  public ConsumerRouteRegistration(final Class<T> packetClass, final Consumer<T> consumer) {
    super(packetClass);

    this.consumer = consumer;
  }

  /**
   * Gets the {@link Consumer} responsible for handling packets associated with this route.
   *
   * @return the consumer tied to this route registration
   */
  public Consumer<T> getConsumer() {
    return consumer;
  }
}
