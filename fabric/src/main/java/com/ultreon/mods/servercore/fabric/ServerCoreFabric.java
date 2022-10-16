package com.ultreon.mods.servercore.fabric;

import com.ultreon.mods.servercore.ServerCore;
import net.fabricmc.api.ModInitializer;

public class ServerCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerCore.init();
    }
}