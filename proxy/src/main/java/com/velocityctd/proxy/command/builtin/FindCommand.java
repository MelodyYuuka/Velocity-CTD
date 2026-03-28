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
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.server.VelocityRegisteredServer;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.translation.Argument;

/**
 * Implements Velocity-CTD's {@code /find} command.
 */
public class FindCommand implements BuiltinCommand {

  private final VelocityServer server;

  public FindCommand(VelocityServer server) {
    this.server = server;
  }

  @Override
  public String label() {
    return "find";
  }

  @Override
  public BrigadierCommand build() {
    LiteralArgumentBuilder<CommandSource> rootNode = BrigadierCommand
        .literalArgumentBuilder(label())
        .requires(source -> source.getPermissionValue("velocity.command.find") == Tristate.TRUE)
        .executes(ctx -> CommandUtils.emitUsage(ctx, label()));
    RequiredArgumentBuilder<CommandSource, String> playerNode = BrigadierCommand
        .requiredArgumentBuilder("player", StringArgumentType.word())
        .suggests((ctx, builder) -> CommandUtils.suggestPlayer(server, ctx, builder, true))
        .executes(this::find);

    rootNode.then(playerNode);
    return new BrigadierCommand(rootNode);
  }

  private int find(CommandContext<CommandSource> context) {
    if (server.isRedisEnabled()) {
      return findRedis(context);
    }

    String player = context.getArgument("player", String.class);
    Optional<ConnectedPlayer> maybePlayer = server.getPlayer(player);
    if (maybePlayer.isEmpty()) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", player))
      );

      return 0;
    }

    // Can't be null, already checking if it's empty before
    ConnectedPlayer p = maybePlayer.get();
    VelocityServerConnection connection = p.getCurrentServer().orElse(null);
    if (connection == null) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", player))
      );

      return 0;
    }

    VelocityRegisteredServer server = connection.getServer();
    if (server == null) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", player))
      );

      return 0;
    }

    context.getSource().sendMessage(
        Component.translatable("velocity.command.find.message", NamedTextColor.YELLOW)
            .arguments(
                Argument.string("player", p.getUsername()),
                Argument.string("server", server.getServerInfo().getName())));

    return Command.SINGLE_SUCCESS;
  }

  private int findRedis(CommandContext<CommandSource> context) {
    VelocityRedis redis = server.getRedis();
    String player = context.getArgument("player", String.class);
    if (!redis.getPlayerService().isPlayerOnline(player)) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", player))
      );

      return 0;
    }

    PlayerEntry playerEntry = redis.getPlayerService().getPlayerEntry(player);

    if (playerEntry == null || playerEntry.getServerName() == null) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", player))
      );

      return 0;
    }

    VelocityRegisteredServer server = this.server.getServer(playerEntry.getServerName()).orElse(null);
    if (server == null) {
      context.getSource().sendMessage(
          CommandMessages.PLAYER_NOT_FOUND.arguments(Argument.string("player", player))
      );

      return 0;
    }

    context.getSource().sendMessage(
        Component.translatable("velocity.command.find.message", NamedTextColor.YELLOW)
            .arguments(
                Argument.string("player", playerEntry.getUsername()),
                Argument.string("server", server.getServerInfo().getName() + " (" + playerEntry.getProxyId() + ")")));

    return Command.SINGLE_SUCCESS;
  }
}
