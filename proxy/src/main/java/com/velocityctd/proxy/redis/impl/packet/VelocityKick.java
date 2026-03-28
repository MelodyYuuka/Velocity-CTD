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
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet used to remotely kick a player from another proxy.
 *
 * <p>This packet transports both the unique player identifier and the
 * disconnect message, and is handled as a one-way message across proxies.</p>
 */
@OneWayPacket
public final class VelocityKick extends ComponentPacket {

  /**
   * The unique identifier of the player being kicked.
   */
  private final UUID uniqueId;

  /**
   * The proxy ID that should process this kick, or {@code null} if all proxies should process it.
   */
  private final @Nullable String targetProxyId;

  /**
   * Constructs a new {@link VelocityKick} packet that targets all proxies.
   *
   * @param uniqueId the player's unique ID
   * @param component the message to send
   */
  public VelocityKick(final UUID uniqueId, final Component component) {
    this(uniqueId, component, null);
  }

  /**
   * Constructs a new {@link VelocityKick} packet targeted at a specific proxy.
   *
   * @param uniqueId the player's unique ID
   * @param component the message to send
   * @param targetProxyId the proxy that should process this kick, or {@code null} for all proxies
   */
  public VelocityKick(final UUID uniqueId, final Component component, final @Nullable String targetProxyId) {
    super(component);

    this.uniqueId = uniqueId;
    this.targetProxyId = targetProxyId;
  }

  /**
   * Gets the player's unique ID.
   *
   * @return the player's unique ID
   */
  public UUID getUniqueId() {
    return uniqueId;
  }

  /**
   * Gets the target proxy ID for this kick.
   *
   * @return the target proxy ID, or {@code null} if all proxies should process this kick
   */
  public @Nullable String getTargetProxyId() {
    return targetProxyId;
  }
}
