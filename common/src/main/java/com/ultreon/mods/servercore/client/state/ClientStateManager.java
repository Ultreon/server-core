package com.ultreon.mods.servercore.client.state;

import dev.architectury.event.events.client.ClientPlayerEvent;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Client state manager.
 *
 * @since 0.1.0
 */
public class ClientStateManager {
    private static ClientStateManager instance;
    private MultiplayerState multiplayer;
    private final LocalState local = new LocalState();

    private ClientStateManager() {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(this::onJoin);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(this::onQuit);
    }

    /**
     * Get the instance of the manager.
     *
     * @return the instance.
     * @since 0.1.0
     */
    public static ClientStateManager get() {
        return instance;
    }

    /**
     * Get the multiplayer state.
     *
     * @return the state.
     * @since 0.1.0
     */
    @Nullable
    public MultiplayerState getMultiplayer() {
        return multiplayer;
    }

    /**
     * Initialize the manager.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        instance = new ClientStateManager();
    }

    /**
     * Handle joining of servers.
     *
     * @param localPlayer player joined.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    private void onJoin(LocalPlayer localPlayer) {
        multiplayer = new MultiplayerState(localPlayer);
    }

    /**
     * Handle leaving on servers.
     *
     * @param localPlayer player left.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    private void onQuit(@Nullable LocalPlayer localPlayer) {
        multiplayer = null;
    }
}
