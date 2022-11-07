package com.ultreon.mods.servercore.server.teleport;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Teleport request handler.
 *
 * @author Qboi123
 */
public interface RequestHandler {
    /**
     * Get the handler's name.
     *
     * @return the name.
     */
    Component getObjName();

    /**
     * Get the handler's ID.
     *
     * @return the UUID.
     */
    UUID getId();

    /**
     * Check if the request handler is online.
     *
     * @return whether the handler is online.
     */
    boolean isOnline();

    /**
     * Check if the handler's teleport status is static.
     * Returns true if the handler can't teleport somewhere else.
     *
     * @return whether the teleport status is static.
     */
    boolean isStatic();

    /**
     * Does nothing if {@link #isStatic()} returns true.
     *
     * @param destination destination to teleport to.
     */
    void teleportTo(Entity destination);

    /**
     * Does nothing if {@link #isStatic()} returns true.
     *
     * @param destination destination to teleport to.
     */
    void teleportTo(Vec3 destination);

    /**
     * The handler's current getObjPos.
     *
     * @return the getObjPos.
     */
    Vec3 getObjPos();
}
