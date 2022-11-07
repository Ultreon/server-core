package com.ultreon.mods.servercore.entity;

import com.ultreon.mods.servercore.server.teleport.TeleportRequest;

public interface RespondsToTpa {
    void receive(TeleportRequest request);
}
