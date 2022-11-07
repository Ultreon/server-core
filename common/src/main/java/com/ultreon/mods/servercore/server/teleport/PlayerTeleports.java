package com.ultreon.mods.servercore.server.teleport;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

public interface PlayerTeleports extends PlayerRecipient, RequestSender {

    boolean cancelRequest(UUID request);

    boolean denyRequest(UUID request);

    boolean acceptRequest(UUID request);

    void cancelTp();

    @ApiStatus.Internal
    void onLeft();

    @Override
    default Vec3 getObjPos() {
        System.out.println("Player Teleports Object Pos");
        return PlayerRecipient.super.getObjPos();
    }
}
