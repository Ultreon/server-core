package com.ultreon.mods.servercore.server.teleport;

import com.mojang.authlib.GameProfile;
import com.ultreon.mods.servercore.server.ServerHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ClassCanBeRecord")
public class TeleportManager {
    static final Map<UUID, TeleportRequest> ACTIVE_REQUESTS = new ConcurrentHashMap<>();
    private static TeleportManager instance;
    private final MinecraftServer server;

    @ApiStatus.Internal
    public TeleportManager(MinecraftServer server) {
        this.server = server;
    }

    public static TeleportManager get() {
        return instance;
    }

    public static UUID nextReqId() {
        UUID uuid;

        do {
            uuid = UUID.randomUUID();
        } while (ACTIVE_REQUESTS.containsKey(uuid));

        return uuid;
    }

    @ApiStatus.Internal
    public static void terminateRequest(UUID id) {
        ACTIVE_REQUESTS.remove(id);
    }

    @ApiStatus.Internal
    public static void registerRequest(TeleportRequest request) {
        ACTIVE_REQUESTS.put(request.id(), request);
    }

    public PlayerTeleports get(UUID playerId) {
        return get(ServerHooks.player(playerId));
    }

    public PlayerTeleports get(Player player) {
        return cast(player);
    }

    public PlayerTeleports get(GameProfile player) {
        return get(player.getId());
    }

    private PlayerTeleports cast(Player player) {
        return ((PlayerTeleports)player);
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
