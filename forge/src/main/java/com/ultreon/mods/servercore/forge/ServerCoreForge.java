package com.ultreon.mods.servercore.forge;

import com.ultreon.mods.servercore.ServerCore;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Main mod class for Forge.
 *
 * @since 0.1.0
 */
@Mod(ServerCore.MOD_ID)
public class ServerCoreForge {
    /**
     * Initialize the mod.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public ServerCoreForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ServerCore.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ServerCore.init();
    }
}