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

package com.velocityctd.proxy.redis.impl.packet;

import com.velocityctd.proxy.redis.packet.annotation.OneWayPacket;
import com.velocityctd.proxy.redis.packet.typed.ComponentPacket;
import net.kyori.adventure.text.Component;

/**
 * Represents a packet that sends an alert message to all proxies.
 */
@OneWayPacket
public final class VelocityAlert extends ComponentPacket {

  /**
   * Constructs a new {@link VelocityAlert} packet.
   *
   * @param component the message to send.
   */
  public VelocityAlert(final Component component) {
    super(component);
  }
}
