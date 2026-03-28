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

package com.velocityctd.proxy.redis.impl;

import com.velocityctd.proxy.redis.packet.RedisPacket;
import com.velocityctd.proxy.redis.packet.annotation.OneWayPacket;
import com.velocityctd.proxy.redis.packet.typed.ComponentPacket;
import com.velocityctd.proxy.redis.transaction.Transaction;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

/**
 * Represents a set of default behaviours for a specific packet, which is used to define how the packet should behave,
 * for example, when an {@link Transaction} is about to be
 * completed, or when a {@link OneWayPacket} is received.
 *
 * @param <C> the type of carrier
 * @param <T> the type of packet
 */
public interface PacketBehaviour<C, T extends RedisPacket> {

  /**
   * Used whenever the Component payload is sent to any audience.
   */
  PacketBehaviour<Audience, ComponentPacket> SEND_COMPONENT = (carrier, packet) -> {
    final Component component = packet.deserialize();
    if (component != null) {
      carrier.sendMessage(component);
    }
  };

  /**
   * Method used to let the desired packet 'behave', with a given carrier.
   *
   * @param carrier the carrier used to support this behaviour
   * @param packet  the packet to have a behaviour
   */
  void behave(C carrier, T packet);
}
