package com.ultreon.mods.servercore.server.teleport;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Teleport recipient handler.
 *
 * @param <T> the recipient type.
 * @author Qboi123
 */
public interface RequestRecipient<T> extends RequestHandler {
    /**
     * Recipient instance.
     *
     * @return the instance.
     */
    @Nullable
    T recipient();

    /**
     * Recipient's getObjPos.
     *
     * @return the getObjPos.
     */
    Vec3 getObjPos();

    /**
     * Receive an inbound teleport request.
     *
     * @param request the request to handle.
     */
    void receiveInbound(TeleportRequest request);

    /**
     * Receive an outbound teleport request.
     *
     * @param request the request to handle.
     */
    void receiveOutbound(TeleportRequest request);

    /**
     * Get the recipient as teleport destination.
     *
     * @return the teleport destination.
     */
    default TeleportDestination<?> asDestination() {
        return TeleportDestination.pos(getObjPos());
    }

    /**
     * Does nothing if {@link #isStatic()} returns true.
     *
     * @param sender the sender to teleport to.
     */
    void teleportTo(RequestSender sender);
}
