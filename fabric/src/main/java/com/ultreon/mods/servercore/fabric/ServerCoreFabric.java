package com.ultreon.mods.servercore.fabric;

import com.ultreon.mods.servercore.ServerCore;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Main mod class for Fabric.
 *
 * @since 0.1.0
 */
public class ServerCoreFabric implements ModInitializer {
    /**
     * Initialize the mod/
     *
     * @since 0.1.0
     */
    @Override
    @ApiStatus.Internal
    public void onInitialize() {
        ServerCore.init();
    }
}