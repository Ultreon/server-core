package com.ultreon.mods.servercore.client;

import com.ultreon.mods.servercore.client.state.ClientStateManager;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

/**
 * Client events class.
 * Handles client-side events.
 *
 * @since 0.1.0
 */
public class ClientEvents {
    private static ClientEvents instance;

    private ClientEvents() {
        ClientLifecycleEvent.CLIENT_SETUP.register(this::setup);
    }

    /**
     * Get the instance of the class.
     *
     * @return the instance.
     * @since 0.1.0
     */
    public static ClientEvents get() {
        return instance;
    }

    private void setup(Minecraft minecraft) {
        ClientStateManager.init();
    }

    /**
     * Initialize the client event handlers.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        instance = new ClientEvents();
    }
}
