package com.ultreon.mods.servercore;

import com.ultreon.mods.servercore.client.ClientEvents;
import com.ultreon.mods.servercore.init.ModDebugGameRules;
import com.ultreon.mods.servercore.network.Network;
import com.ultreon.mods.servercore.server.ServerEvents;
import com.ultreon.mods.servercore.server.config.Config;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
        // Initialize events handler classes.
        EnvExecutor.runInEnv(Env.CLIENT, () -> ClientEvents::init);
        ServerEvents.init();

        // Init some registration classes.
        if (Platform.isDevelopmentEnvironment()) {
            ModDebugGameRules.initNop();
        }

        // Listen to the common setup event.
        LifecycleEvent.SETUP.register(ServerCore::setup);

        Config.init();
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

    @Nullable
    public static Entity getEntity(MinecraftServer server, UUID entity) {
        for (ServerLevel level : server.getAllLevels()) return level.getEntity(entity);
        return null;
    }
}