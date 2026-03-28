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

package com.velocityctd.proxy.queue.redis.packet;

import com.velocityctd.api.queue.QueueState;
import com.velocityctd.api.queue.ServerStatus;
import com.velocityctd.proxy.redis.packet.GenericPacket;
import com.velocityctd.proxy.redis.packet.annotation.OneWayPacket;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * A Redis pub/sub packet used to synchronize queue state changes across all proxies.
 *
 * <p>This packet is published whenever the queue state changes on any proxy (player enqueued,
 * dequeued, server status changed, queue state changed, or waitingForConnection flag updated).
 * All proxies receive the packet and apply the change to their local in-memory queue, ensuring
 * consistent queue state across the cluster without full-overwrite race conditions.</p>
 */
@OneWayPacket
public final class VelocityQueueSync extends GenericPacket<VelocityQueueSync.Payload> {

  /**
   * The action that triggered this sync packet.
   */
  public enum Action {
    /**
     * A player was added to the queue.
     */
    ENQUEUE,
    /**
     * A player was removed from the queue.
     */
    DEQUEUE,
    /**
     * The queue operational state changed (ACTIVE/PAUSED/INACTIVE).
     */
    STATE_CHANGE,
    /**
     * The backend server status changed (ONLINE/OFFLINE/WAITING/FULL).
     */
    STATUS_CHANGE,
    /**
     * A player's waitingForConnection flag, connection attempt count, and permissions were updated.
     */
    WAITING_CHANGE
  }

  /**
   * The payload carried by a {@link VelocityQueueSync} packet.
   *
   * <p>Fields are nullable when not applicable to the specific action.</p>
   */
  public record Payload(
      Action action,
      String serverName,
      @Nullable UUID playerUuid,
      // ENQUEUE fields
      @Nullable String username,
      int priority,
      boolean fullBypass,
      boolean queueBypass,
      // STATE_CHANGE field
      @Nullable QueueState newState,
      // STATUS_CHANGE field
      @Nullable ServerStatus newStatus,
      // WAITING_CHANGE fields
      boolean waitingForConnection,
      int connectionAttempts,
      int updatedPriority,
      boolean updatedFullBypass,
      boolean updatedQueueBypass
  ) {

    /**
     * Creates an ENQUEUE payload.
     */
    public static Payload enqueue(String serverName, UUID uuid, String username, int priority,
                                  boolean fullBypass, boolean queueBypass) {
      return new Payload(Action.ENQUEUE, serverName, uuid, username, priority, fullBypass,
          queueBypass, null, null, false, 0, 0, false, false);
    }

    /**
     * Creates a DEQUEUE payload.
     */
    public static Payload dequeue(String serverName, UUID uuid) {
      return new Payload(Action.DEQUEUE,
          serverName, uuid, null, 0, false, false, null, null, false, 0, 0, false, false);
    }

    /**
     * Creates a STATE_CHANGE payload.
     */
    public static Payload stateChange(String serverName, QueueState state) {
      return new Payload(Action.STATE_CHANGE, serverName, null, null, 0, false, false,
          state, null, false, 0, 0, false, false);
    }

    /**
     * Creates a STATUS_CHANGE payload.
     */
    public static Payload statusChange(String serverName, ServerStatus status) {
      return new Payload(Action.STATUS_CHANGE, serverName, null, null, 0, false, false,
          null, status, false, 0, 0, false, false);
    }

    /**
     * Creates a WAITING_CHANGE payload.
     */
    public static Payload waitingChange(String serverName, UUID uuid, boolean waitingForConnection,
                                        int connectionAttempts, int updatedPriority,
                                        boolean updatedFullBypass, boolean updatedQueueBypass) {
      return new Payload(Action.WAITING_CHANGE, serverName, uuid, null, 0, false, false,
          null, null, waitingForConnection, connectionAttempts, updatedPriority,
          updatedFullBypass, updatedQueueBypass);
    }
  }

  /**
   * Constructs a new {@link VelocityQueueSync} packet with the given payload.
   *
   * @param payload the sync payload
   */
  public VelocityQueueSync(final Payload payload) {
    super(payload);
  }
}
