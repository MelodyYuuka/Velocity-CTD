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

import com.velocityctd.proxy.queue.RedisVelocityQueueManager;
import com.velocityctd.proxy.queue.VelocityQueue;
import com.velocityctd.proxy.queue.VelocityQueueEntry;
import com.velocityctd.proxy.queue.redis.packet.VelocityQueueSync;
import com.velocityctd.proxy.queue.redis.packet.VelocityQueueTransfer;
import com.velocityctd.proxy.redis.VelocityRedis;
import com.velocityctd.proxy.redis.impl.packet.VelocityActionBar;
import com.velocityctd.proxy.redis.impl.packet.VelocityAlert;
import com.velocityctd.proxy.redis.impl.packet.VelocityKick;
import com.velocityctd.proxy.redis.impl.packet.VelocityMessage;
import com.velocityctd.proxy.redis.impl.packet.VelocitySudo;
import com.velocityctd.proxy.redis.impl.packet.VelocitySwitchServer;
import com.velocityctd.proxy.redis.packet.RedisPacket;
import com.velocityctd.proxy.redis.registration.RouteRegistration;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a registry that holds all {@link RouteRegistration} for the VelocityRedis module. An
 * internal {@link Function} is used to handle the data and create a response from the data.
 */
public enum RouteRegistry {

  /**
   * Handles the {@link VelocityAlert} packet by sending a message to all players on the proxy.
   */
  VELOCITY_ALERT(VelocityAlert.class, PacketBehaviour.SEND_COMPONENT::behave),

  /**
   * Handles the {@link VelocitySwitchServer} packet by switching the player to the specified server.
   */
  VELOCITY_SWITCH_SERVER(VelocitySwitchServer.class, (server, packet) -> {
    final ConnectedPlayer player = server.getPlayer(packet.getUsername()).orElse(null);
    if (player == null) {
      return;
    }

    server.getServer(packet.getServerName()).ifPresent(targetServer ->
            player.createConnectionRequest(targetServer).connectWithIndication());
  }),

  /**
   * Handles the {@link VelocityMessage} packet by sending a message to the specified target.
   */
  VELOCITY_MESSAGE(VelocityMessage.class, (server, packet) -> packet.sendMessage(server)),

  /**
   * Handles the {@link VelocityActionBar} packet by sending an action bar to the specified target.
   */
  VELOCITY_ACTION_BAR(VelocityActionBar.class, (server, packet) -> {
    final Component component = packet.deserialize();
    if (component == null) {
      return;
    }

    server.getPlayer(packet.getUniqueId()).ifPresent(player -> player.sendActionBar(component));
  }),

  /**
   * Handles the {@link VelocitySudo} packet by letting the specified player execute a command or chat message.
   */
  VELOCITY_SUDO(VelocitySudo.class, (server, packet) -> {
    final ConnectedPlayer player = server.getPlayer(packet.getPayload()).orElse(null);
    if (player == null) {
      return;
    }

    final String message = packet.getMessage();
    if (message.startsWith("/")) {
      final String fullCommand = message.substring(1);
      final String commandLabel = fullCommand.split(" ")[0];
      if (server.getCommandManager().hasCommand(commandLabel)) {
        server.getCommandManager().executeAsync(player, fullCommand);
        return;
      }
    }

    player.spoofChatInput(message);
  }),

  /**
   * Handles the {@link VelocityKick} packet by kicking the specified player with a reason.
   */
  VELOCITY_KICK(VelocityKick.class, (server, packet) -> {
    final String targetProxyId = packet.getTargetProxyId();
    if (targetProxyId != null && !targetProxyId.equalsIgnoreCase(server.getRedis().getProxyId())) {
      return;
    }

    final ConnectedPlayer player = server.getPlayer(packet.getUniqueId()).orElse(null);
    if (player == null) {
      return;
    }

    Component component = packet.deserialize();
    if (component == null) {
      component = Component.text("You have been kicked from the proxy.", NamedTextColor.RED);
    }

    player.disconnect0(component, true);
  }),

  /**
   * Handles the {@link VelocityQueueSync} packet by applying the state change to the local queue.
   */
  VELOCITY_QUEUE_SYNC(VelocityQueueSync.class, (server, packet) -> {
    RedisVelocityQueueManager redisVelocityQueueManager = ((RedisVelocityQueueManager) server.getQueueManager());
    redisVelocityQueueManager.handleSync(packet);
  }),

  /**
   * Handles the {@link VelocityQueueTransfer} packet by transferring the player to their target server.
   */
  VELOCITY_QUEUE(VelocityQueueTransfer.class, (server, packet) -> {
    final VelocityQueue queue;
    try {
      queue = server.getQueueManager().getQueue(packet.getQueueName());
    } catch (IllegalArgumentException ignored) {
      return; // unknown server - stale or malformed packet
    }

    final ConnectedPlayer player = server.getPlayer(packet.getPayload()).orElse(null);
    if (player == null) {
      if (!server.getRedis().getPlayerService().isPlayerOnline(packet.getPayload())) {
        final VelocityQueueEntry entry = queue.getEntry(packet.getPayload());
        if (entry != null) {
          entry.abortTransfer();
        }
      }
      return;
    }

    final VelocityQueueEntry entry = queue.getEntry(packet.getPayload());
    if (entry == null) {
      return;
    }

    entry.handleTransfer();
  });

  /**
   * The {@link RouteRegistration} that defines how this Redis packet type
   * is routed and handled within the proxy.
   */
  private final RouteRegistration<? extends RedisPacket> routeRegistration;

  <T extends RedisPacket> RouteRegistry(final Class<T> packetClass, final @NotNull BiConsumer<VelocityServer, T> route) {
    this.routeRegistration = RouteRegistration.consumer(packetClass, packet -> route.accept(VelocityRedis.INSTANCE.getServer(), packet));
  }

  /**
   * Gets the {@link RouteRegistration} associated with this route entry.
   *
   * @return the route registration for this Redis packet type
   */
  public RouteRegistration<? extends RedisPacket> getRouteRegistration() {
    return routeRegistration;
  }
}
