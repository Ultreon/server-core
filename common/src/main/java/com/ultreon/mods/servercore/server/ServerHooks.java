package com.ultreon.mods.servercore.server;

import com.ultreon.mods.servercore.mixin.AntiMixin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.function.Predicate;

@AntiMixin
public class ServerHooks {
    public static MinecraftServer server() {
        return ServerEvents.server();
    }

    public static Entity entity(UUID recipientId) {
        Entity entity;
        for (ServerLevel level : server().getAllLevels())
            if ((entity = level.getEntity(recipientId)) != null) return entity;

        return null;
    }

    public static Player player(UUID uuid) {
        return server().getPlayerList().getPlayer(uuid);
    }

    /**
     * Check if an entity exists with a specific UUID.
     *
     * @param id the UUID to check for.
     * @return whether an entity exists.
     */
    public static boolean entityExists(UUID id) {
        for (ServerLevel level : server().getAllLevels())
            if (level.getEntity(id) != null) return true;

        return false;
    }

    /**
     * Check if an entity exists in a given predicate.
     *
     * @param predicate the checker.
     * @return whether an entity exists.
     */
    public static boolean entityExists(Predicate<Entity> predicate) {
        for (ServerLevel level : server().getAllLevels())
            for (Entity entity : level.getAllEntities())
                if (predicate.test(entity)) return true;

        return false;
    }

    /**
     * Check if an entity exists of a given type.
     *
     * @param type the type of entity.
     * @return whether an entity exists.
     */
    public static boolean entityExists(EntityType<?> type) {
        return entityExists(entity -> entity.getType().equals(type));
    }
}
