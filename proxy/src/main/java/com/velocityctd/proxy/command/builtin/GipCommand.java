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

package com.velocityctd.proxy.command.builtin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocityctd.proxy.command.CommandUtils;
import com.velocityctd.proxy.redis.VelocityRedis;
import com.velocityctd.proxy.redis.impl.depot.PlayerEntry;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.command.builtin.BuiltinCommand;
import com.velocitypowered.proxy.command.builtin.CommandMessages;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;

/**
 * Implements Velocity-CTD's {@code /gip} command.
 */
public class GipCommand implements BuiltinCommand {

  private final VelocityServer server;

  public GipCommand(final VelocityServer server) {
    this.server = server;
  }

  @Override
  public String label() {
    return "gip";
  }

  @Override
  public BrigadierCommand build() {
    RequiredArgumentBuilder<CommandSource, String> playerNode = BrigadierCommand
        .requiredArgumentBuilder("player", StringArgumentType.word())
        .suggests((ctx, builder) -> CommandUtils.suggestPlayer(server, ctx, builder, true))
        .executes(this::executeIp);

    LiteralArgumentBuilder<CommandSource> rootNode = BrigadierCommand
        .literalArgumentBuilder(label())
        .requires(source -> source.getPermissionValue("velocity.command.gip") == Tristate.TRUE)
        .executes(ctx -> CommandUtils.emitUsage(ctx, label()))
        .then(playerNode);

    return new BrigadierCommand(rootNode);
  }

  private int executeIp(final CommandContext<CommandSource> context) {
    if (server.isRedisEnabled()) {
      return executeIpRedis(context);
    }

    return executeIpLocal(context);
  }

  private int executeIpLocal(final CommandContext<CommandSource> context) {
    final String playerName = context.getArgument("player", String.class);
    final ConnectedPlayer player = server.getPlayer(playerName).orElse(null);

    if (player == null) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", playerName))
      );
      return 0;
    }

    final String ip = player.getRemoteAddress().getAddress().getHostAddress();
    context.getSource().sendMessage(
        Component.translatable("velocity.command.gip.message")
            .arguments(
                Argument.string("0", player.getUsername()),
                Argument.string("1", ip)
            )
    );

    return Command.SINGLE_SUCCESS;
  }

  private int executeIpRedis(final CommandContext<CommandSource> context) {
    final VelocityRedis redis = server.getRedis();
    final String playerName = context.getArgument("player", String.class);

    if (!redis.getPlayerService().isPlayerOnline(playerName)) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", playerName))
      );
      return 0;
    }

    final PlayerEntry entry = redis.getPlayerService().getPlayerEntry(playerName);
    if (entry == null || entry.getIpAddress() == null) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", playerName))
      );
      return 0;
    }

    context.getSource().sendMessage(
        Component.translatable("velocity.command.gip.message")
            .arguments(
                Argument.string("0", entry.getUsername()),
                Argument.string("1", entry.getIpAddress())
            )
    );

    return Command.SINGLE_SUCCESS;
  }
}
