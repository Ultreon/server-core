package com.ultreon.mods.servercore.server.teleport;

import com.google.common.collect.Streams;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public abstract class TeleportRequest {
    protected final PlayerTeleports requester;
    private final TeleportManager manager;
    private boolean valid = true;

    public TeleportRequest(PlayerTeleports requester) {
        this.requester = requester;
        this.manager = requester.getManager();
    }

    public static TeleportRequest inbound(UUID destination, PlayerTeleports requester) {
        return new UserReceiver(Type.INBOUND, destination, requester);
    }

    public abstract boolean handle();

    public abstract boolean canTeleport();

    public void invalidate() {
        this.valid = false;
    }

    public boolean stillValid() {
        return valid;
    }

    public TeleportManager getManager() {
        return manager;
    }

    public enum Type {
        INBOUND, OUTBOUND
    }

    public static class UserReceiver extends TeleportRequest {
        private final Type type;
        private final UUID destination;
        private Entity entity;

        public UserReceiver(Type type, UUID destination, PlayerTeleports requester) {
            super(requester);

            this.type = type;
            this.destination = destination;
        }

        @Override
        public boolean handle() {
            if (!validateEntity()) {
                return false;
            }
            if (type == Type.INBOUND) {
                requester.teleportTo(entity);
            }
            return true;
        }

        private boolean validateEntity() {
            for (ServerLevel level : getManager().getServer().getAllLevels()) {
                if ((this.entity = level.getEntity(destination)) != null) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean canTeleport() {
            return validateDestination();
        }

        private boolean validateDestination() {
            if (Streams.stream(getManager().getServer().getAllLevels()).noneMatch(level -> level.getEntity(destination) != null))
                invalidate();
            return stillValid();
        }
    }
}
