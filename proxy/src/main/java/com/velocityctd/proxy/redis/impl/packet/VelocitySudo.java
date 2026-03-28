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
import com.velocityctd.proxy.redis.packet.typed.UuidPacket;
import java.util.UUID;

/**
 * Represents a packet that sends a sudo action for a player.
 */
@OneWayPacket
public final class VelocitySudo extends UuidPacket {

  /**
   * The message or command that will be executed on behalf of the player.
   */
  private final String message;

  /**
   * Constructs a new {@link VelocitySudo} packet.
   *
   * @param playerUniqueId the player's unique ID
   * @param message the message/command to send
   */
  public VelocitySudo(final UUID playerUniqueId, final String message) {
    super(playerUniqueId);
    this.message = message;
  }

  /**
   * Gets the message/command to send.
   *
   * @return the message/command to send
   */
  public String getMessage() {
    return message;
  }
}
