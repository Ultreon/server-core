package com.ultreon.mods.servercore.server;

import com.ultreon.mods.servercore.server.state.ServerStateManager;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
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
        EntityEvent.ADD.register(this::onAddEntity);
        PlayerEvent.PLAYER_QUIT.register(this::onQuit);
    }

    private EventResult onAddEntity(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            this.onJoin(player);
        }
        return EventResult.pass();
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
