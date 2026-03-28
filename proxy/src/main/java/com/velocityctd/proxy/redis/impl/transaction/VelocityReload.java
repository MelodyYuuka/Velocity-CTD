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

package com.velocityctd.proxy.redis.impl.transaction;

import com.velocityctd.proxy.redis.impl.PacketBehaviour;
import com.velocityctd.proxy.redis.packet.typed.ComponentPacket;
import com.velocityctd.proxy.redis.packet.typed.StringPacket;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a transaction that reloads any proxy.
 */
public final class VelocityReload extends VelocityTransaction<StringPacket, ComponentPacket> {

  /**
   * Constructs a new {@link VelocityReload} transaction.
   *
   * @param source the command source to send the result to
   * @param proxyId the id of the proxy to reload
   */
  public VelocityReload(final @NotNull CommandSource source, final @NotNull String proxyId) {
    super(new StringPacket(proxyId), source, "redis.command.reload.timeout");

    this.onComplete(packet -> PacketBehaviour.SEND_COMPONENT.behave(source, packet));
  }
}
