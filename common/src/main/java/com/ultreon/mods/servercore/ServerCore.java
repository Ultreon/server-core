package com.ultreon.mods.servercore;

import com.ultreon.mods.servercore.client.ClientEvents;
import com.ultreon.mods.servercore.network.Network;
import com.ultreon.mods.servercore.server.ServerEvents;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class.
 *
 * @since 0.1.0
 */
public class ServerCore {
    /**
     * The mod's ID.
     *
     * @since 0.1.0
     */
    public static final String MOD_ID = "servercore";

    /**
     * Mod's Logger
     *
     * @since 0.1.0
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("ServerCore");

    /**
     * Initialize the mod.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> ClientEvents::init);
        ServerEvents.init();

        LifecycleEvent.SETUP.register(ServerCore::setup);
    }

    private static void setup() {
        Network.init(); // Initialized here to not cause a deadlock.
    }

    /**
     * Get a resource location / identifier instance from a path.
     * The namespace is defaulted to {@link #MOD_ID}
     *
     * @param path the resource location path.
     * @return the resource location.
     * @since 0.1.0
     */
    public static ResourceLocation res(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}