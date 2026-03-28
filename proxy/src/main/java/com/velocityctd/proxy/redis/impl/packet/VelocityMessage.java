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

import com.velocityctd.proxy.redis.impl.model.EncodedCommandSource;
import com.velocityctd.proxy.redis.packet.annotation.OneWayPacket;
import com.velocityctd.proxy.redis.packet.typed.ComponentPacket;
import com.velocitypowered.proxy.VelocityServer;
import java.util.UUID;
import net.kyori.adventure.text.Component;

/**
 * Represents a packet that sends a message to a player or a command source.
 */
@OneWayPacket
public final class VelocityMessage extends ComponentPacket {

  /**
   * The encoded representation of the command source that should receive this message,
   * or {@code null} if the message targets a specific player instead.
   */
  private EncodedCommandSource commandSource = null;

  /**
   * The unique identifier of the player who should receive this message,
   * or {@code null} if the message targets a command source instead.
   */
  private UUID playerUniqueId = null;

  /**
   * Constructs a new {@link VelocityMessage} packet.
   *
   * @param commandSource the command source to send the message to
   * @param component the message to send
   */
  public VelocityMessage(final EncodedCommandSource commandSource, final Component component) {
    super(component);
    this.commandSource = commandSource;
  }

  /**
   * Constructs a new {@link VelocityMessage} packet.
   *
   * @param playerUniqueId the player's unique ID to send the message to
   * @param component the message to send
   */
  public VelocityMessage(final UUID playerUniqueId, final Component component) {
    super(component);
    this.playerUniqueId = playerUniqueId;
  }

  /**
   * Sends the message to the specified server.
   *
   * @param server the server to send the message to
   */
  public void sendMessage(final VelocityServer server) {
    final Component component = this.deserialize();
    if (component == null) {
      return;
    }

    if (this.playerUniqueId != null) {
      server.getPlayer(this.playerUniqueId).ifPresent(player -> {
        player.sendMessage(component);
      });
    } else if (this.commandSource != null) {
      this.commandSource.sendMessage(server, component);
    }
  }
}
