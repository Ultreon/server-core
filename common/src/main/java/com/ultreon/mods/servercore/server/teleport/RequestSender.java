package com.ultreon.mods.servercore.server.teleport;

import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * Teleport request sender.
 *
 * @author Qboi123
 */
public interface RequestSender extends RequestHandler {
    /**
     * Request teleport to another entity.
     *
     * @param uuid the entity's id.
     */
    default void requestTeleportTo(UUID uuid) {
        requestTeleportTo(EntityRecipient.entity(uuid));
    }

    /**
     * Request teleport of another entity to {@code this}.
     *
     * @param uuid the entity's id.
     */
    default void requestTeleportFrom(UUID uuid) throws IllegalRecipientException {
        requestTeleportFrom(EntityRecipient.entity(uuid));
    }

    /**
     * Request teleport to an recipient.
     *
     * @param recipient the recipient to teleport to.
     */
    void requestTeleportTo(RequestRecipient<?> recipient);

    /**
     * Request teleport to an recipient.
     *
     * @param recipient the recipient to teleport.
     */
    void requestTeleportFrom(RequestRecipient<?> recipient) throws IllegalRecipientException;

    /**
     * Handle a sent request/
     *
     * @param request the request to handle.
     * @param type    the type of request.
     */
    void handleSentRequest(TeleportRequest request, TeleportRequest.Type type);

    /**
     * Teleport to a recipient.
     * @param recipient the recipient.
     */
    void teleportTo(RequestRecipient<?> recipient);

    /**
     * Send an error to the sender.
     * @param message the error message.
     */
    void error(String message);
}
