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

import com.velocityctd.proxy.redis.packet.RedisPacket;
import com.velocityctd.proxy.redis.transaction.Transaction;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an extension of the {@link Transaction} for the VelocityRedis module.
 *
 * <p>This provides Velocity-specific behavior for Redis request/response
 * transactions, including automatic timeout messaging to a {@link CommandSource}
 * when applicable.</p>
 *
 * @param <T> the type of Redis packet being sent
 * @param <R> the type of Redis packet expected in response
 */
public abstract class VelocityTransaction<T extends RedisPacket, R extends RedisPacket> extends Transaction<T, R> {

  /**
   * Constructs a new {@link VelocityTransaction}.
   *
   * @param sentPacket the packet to send.
   * @param source the command source to send the timeout message to.
   * @param timeoutTranslationKey the translation key to use for the timeout message.
   */
  public VelocityTransaction(final @NotNull T sentPacket, final @Nullable CommandSource source, final @Nullable String timeoutTranslationKey) {
    super(sentPacket);

    if (source != null && timeoutTranslationKey != null) {
      this.onTimeout(t -> source.sendMessage(Component.translatable(timeoutTranslationKey, NamedTextColor.RED)));
    }
  }

  /**
   * Constructs a new {@link VelocityTransaction} with no command source and translation key.
   *
   * @param sentPacket the packet to send.
   */
  public VelocityTransaction(final @NotNull T sentPacket) {
    this(sentPacket, null, null);
  }
}
