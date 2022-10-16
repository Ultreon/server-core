package com.ultreon.mods.servercore.forge;

import com.ultreon.mods.servercore.ServerCore;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ServerCore.MOD_ID)
public class ServerCoreForge {
    public ServerCoreForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ServerCore.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ServerCore.init();
    }
}