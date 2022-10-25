package com.ultreon.mods.servercore.server.teleport;

import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.server.ServerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public abstract class TeleportDestination<T> {
    public abstract T getDestination();

    public abstract void teleportHere(Entity entity);

    public static TeleportDestination<Vec3> pos(Vec3 vec) {
        return new PosDest(vec);
    }

    public static TeleportDestination<Vec3> pos(double x, double y, double z) {
        return new PosDest(new Vec3(x, y, z));
    }

    public static TeleportDestination<Entity> entity(Entity entity) {
        return new EntityDest(entity, false);
    }

    public static TeleportDestination<Entity> entity(Entity entity, boolean includeRotation) {
        return new EntityDest(entity, includeRotation);
    }

    public static TeleportDestination<Entity> entity(UUID entity) {
        MinecraftServer server = ServerEvents.server();
        if (server == null) throw new IllegalStateException("Server is offline.");
        return new EntityDest(ServerCore.getEntity(server, entity), false);
    }

    public static TeleportDestination<Entity> entity(UUID entity, boolean includeRotation) {
        MinecraftServer server = ServerEvents.server();
        if (server == null) throw new IllegalStateException("Server is offline.");
        return new EntityDest(ServerCore.getEntity(server, entity), includeRotation);
    }

    private static class PosDest extends TeleportDestination<Vec3> {
        private final Vec3 vec3;

        PosDest(Vec3 vec3) {
            this.vec3 = vec3;
        }

        @Override
        public Vec3 getDestination() {
            return vec3;
        }

        @Override
        public void teleportHere(Entity entity) {
            entity.teleportToWithTicket(vec3.x, vec3.y, vec3.z);
        }
    }

    private static class EntityDest extends TeleportDestination<Entity> {
        private final Entity entity;
        private final boolean rotationIncluded;

        EntityDest(Entity entity, boolean includeRotation) {
            this.entity = entity;
            this.rotationIncluded = includeRotation;
        }

        @Override
        public Entity getDestination() {
            return entity;
        }

        @Override
        public void teleportHere(Entity entity) {
            entity.teleportToWithTicket(this.entity.getX(), this.entity.getY(), this.entity.getZ());

            if (isRotationIncluded()) {
                entity.setXRot(this.entity.getXRot());
                entity.setYRot(this.entity.getYRot());
            }
        }

        public boolean isRotationIncluded() {
            return rotationIncluded;
        }
    }
}
