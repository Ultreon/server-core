package com.ultreon.mods.servercore.server;

import com.ultreon.mods.servercore.server.state.ServerStateManager;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Server events class.
 * Handles server-side events.
 *
 * @since 0.1.0
 */
public class ServerEvents {
    private static ServerEvents instance;

    private ServerEvents() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::start);
        LifecycleEvent.SERVER_STOPPED.register(this::stop);
        PlayerEvent.PLAYER_JOIN.register(this::onJoin);
        PlayerEvent.PLAYER_QUIT.register(this::onQuit);
    }

    private void onJoin(ServerPlayer player) {
        final ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            manager.player(player).onJoin(player);
        }
    }

    private void onQuit(ServerPlayer player) {
        final ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            manager.player(player).onQuit();
        }
    }

    /**
     * Get the instance of the server events class.
     *
     * @return the instance.
     * @since 0.1.0
     */
    public static ServerEvents get() {
        return instance;
    }

    private void start(MinecraftServer server) {
        ServerStateManager.start(server);
    }

    private void stop(MinecraftServer server) {
        ServerStateManager.stop();
    }

    /**
     * Initialize the server events handlers.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        instance = new ServerEvents();
    }
}
