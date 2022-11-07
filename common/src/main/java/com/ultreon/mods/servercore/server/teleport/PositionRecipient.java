package com.ultreon.mods.servercore.server.teleport;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface PositionRecipient extends RequestRecipient<Vec3> {
    @Override
    default boolean isStatic() {
        return true;
    }

    @Override
    @Nullable
    default Vec3 recipient() {
        return getObjPos();
    }

    @Override
    default void teleportTo(Vec3 destination) {

    }

    @Override
    default void teleportTo(Entity destination) {

    }

    @Override
    default void teleportTo(RequestSender sender) {

    }

    @Override
    default TeleportDestination<?> asDestination() {
        return TeleportDestination.pos(recipient());
    }
}
