package com.ultreon.mods.servercore.server.teleport;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private static TeleportManager instance;
    private final Map<UUID, PlayerTeleports> playerTp = new HashMap<>();
    private final MinecraftServer server;

    @ApiStatus.Internal
    public TeleportManager(MinecraftServer server) {
        this.server = server;
    }

    public static TeleportManager get() {
        return instance;
    }

    public PlayerTeleports get(UUID playerId) {
        return getOrCreate(playerId);
    }

    public PlayerTeleports get(Player player) {
        return getOrCreate(player.getUUID());
    }

    public PlayerTeleports get(GameProfile player) {
        return getOrCreate(player.getId());
    }

    private PlayerTeleports getOrCreate(UUID playerId) {
        return playerTp.computeIfAbsent(playerId, id -> new PlayerTeleports(this, id));
    }

    @ApiStatus.Internal
    public static void start(MinecraftServer server) {
        instance = new TeleportManager(server);
    }

    @ApiStatus.Internal
    public static void stop() {
        instance = null;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
