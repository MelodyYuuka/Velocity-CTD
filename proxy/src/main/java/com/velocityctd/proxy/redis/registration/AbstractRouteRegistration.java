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
import com.velocityctd.proxy.redis.provider.RedisProvider;

/**
 * Represents an abstract {@link RouteRegistration} for a specific {@link RedisPacket} type.
 *
 * <p>This class stores the packet class associated with the route so that the
 * {@link RedisProvider} can look up the correct
 * handler based on incoming packet type information.</p>
 *
 * @param <T> the type of {@link RedisPacket} handled by this registration
 */
public abstract sealed class AbstractRouteRegistration<T extends RedisPacket> implements RouteRegistration<T>
        permits ConsumerRouteRegistration {

  /**
   * The class type of the {@link RedisPacket} associated with this route registration.
   */
  private final Class<T> packetClass;

  /**
   * Constructs a new {@link AbstractRouteRegistration}.
   *
   * @param packetClass the concrete packet class this registration handles
   */
  public AbstractRouteRegistration(final Class<T> packetClass) {
    this.packetClass = packetClass;
  }

  /**
   * Gets the packet class associated with this route registration.
   *
   * @return the packet class that this registration handles
   */
  @Override
  public Class<T> getPacketClass() {
    return packetClass;
  }
}
