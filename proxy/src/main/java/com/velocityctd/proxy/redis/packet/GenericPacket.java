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

/**
 * Represents a generic Redis packet whose payload type is not predetermined.
 *
 * <p>This class is used to wrap arbitrary data objects as Redis packets,
 * particularly when serialization or deserialization occurs without a
 * strongly typed packet class.</p>
 *
 * @param <T> the type of the payload carried by this packet
 */
public non-sealed class GenericPacket<T> extends AbstractRedisPacket {

  /**
   * The payload stored within this packet. Its type is generic and determined
   * at construction time.
   */
  protected final T payload;

  /**
   * Constructs a new {@link GenericPacket}.
   *
   * @param payload the payload of the packet
   */
  public GenericPacket(final T payload) {
    super();
    this.payload = payload;
  }

  /**
   * Get the payload of the packet.
   *
   * @return the payload of the packet
   */
  public T getPayload() {
    return payload;
  }
}
