package com.ultreon.mods.servercore.server.teleport;

import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.mods.servercore.server.ScheduledTask;
import com.ultreon.mods.servercore.server.TaskManager;
import com.ultreon.mods.servercore.server.chat.ChatContext;
import com.ultreon.mods.servercore.server.config.Config;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A teleport request.
 *
 * @author Qboi123
 */
public class TeleportRequest {
    /**
     * The request type.
     */
    public final Type type;

    /**
     * The sender of the request.
     */
    protected final RequestSender sender;

    /**
     * The recipient of the request/
     */
    protected final RequestRecipient<?> recipient;
    private boolean valid = true;
    private final UUID id;
    private final List<Runnable> onTimeOut = new ArrayList<>();
    private final List<Runnable> onDeny = new ArrayList<>();
    private final List<Runnable> onAccept = new ArrayList<>();
    private final List<Runnable> onFinalize = new ArrayList<>();
    private UUID timeoutToken;

    /**
     * Create a teleport request.
     *
     * @param type      whether to be in- or outbound.
     * @param recipient the recipient.
     * @param sender    the sender.
     */
    public TeleportRequest(UUID id, Type type, RequestRecipient<?> recipient, RequestSender sender) {
        this.id = id;
        this.type = type;
        this.recipient = recipient;
        this.sender = sender;

        onFinalize(() -> TeleportManager.terminateRequest(id));
    }

    /**
     * Create a request which is inbound is the PoV of the recipient.
     *
     * @param recipient the recipient.
     * @param sender    the sender.
     * @return the teleport request/
     */
    public static TeleportRequest inbound(UUID id, RequestRecipient<?> recipient, RequestSender sender) {
        return new TeleportRequest(id, Type.INBOUND, recipient, sender);
    }

    /**
     * Create a request which is outbound is the PoV of the recipient.
     *
     * @param recipient the recipient.
     * @param sender    the sender.
     * @return the teleport request/
     */
    public static TeleportRequest outbound(UUID id, RequestRecipient<?> recipient, RequestSender sender) {
        return new TeleportRequest(id, Type.OUTBOUND, recipient, sender);
    }

    /**
     * Handle the teleportation.
     *
     * @return whether the teleportation was successful.
     */
    @CheckReturnValue
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean handle() {
        if (!validateEntity()) {
            System.out.println("invalid entity");
            invalidate();
            return false;
        }
        if (!validateSender()) {
            System.out.println("invalid sender");
            invalidate();
            return false;
        }

        System.out.println("type = " + type);
        switch (type) {
            case INBOUND -> sender.teleportTo(recipient);
            case OUTBOUND -> {
                if (recipient.isStatic()) {
                    throw new TeleportError("Recipient is static while still got an outbound teleport request.");
                }
                recipient.teleportTo(sender);
            }
        }

        System.out.println("valid = " + valid);
        invalidate();
        System.out.println("valid = " + valid);
        return true;
    }


    private boolean validateEntity() {
        return recipient.isOnline();
    }

    /**
     * Get teleport message context.
     *
     * @return the context.
     */
    public ChatContext getMessageContext() {
        return new ChatContext()
                .key("recipient-name", recipient.getObjName().getString())
                .key("sender-name", sender.getObjName().getString())
                .key("teleport-id", id);
    }

    /**
     * Get the teleport request's recipient.
     * @return the recipient.
     */
    public RequestRecipient<?> recipient() {
        return recipient;
    }

    /**
     * Invalidate the teleport request.
     */
    public void invalidate() {
        this.valid = false;
    }

    /**
     * Check if the request is still valid.
     * @return whether the request is still valid.
     */
    public boolean stillValid() {
        if (id == null) invalidate();
        if (!recipient.isOnline()) invalidate();
        return valid;
    }

    /**
     * Validate the sender.
     * @return whether the sender is still valid.
     */
    protected final boolean validateSender() {
        return sender.isOnline();
    }

    public UUID senderId() {
        return sender.getId();
    }

    public RequestSender sender() {
        return sender;
    }

    public final UUID id() {
        return id;
    }

    public void timeOut() {
        this.valid = false;
        this.onTimeOut.forEach(Runnable::run);
        this.onFinalize.forEach(Runnable::run);
    }

    public void onTimeOut(Runnable func) {
        this.onTimeOut.add(func);
    }

    public void deny() {
        if (!valid) return;
        TaskManager.INSTANCE.cancelTask(timeoutToken);
        this.valid = false;
        this.onDeny.forEach(Runnable::run);
        this.onFinalize.forEach(Runnable::run);
        TeleportManager.terminateRequest(id);
    }

    public void onDeny(Runnable func) {
        this.onDeny.add(func);
    }

    public void accept() {
        System.out.println("valid = " + valid);
        if (!valid) {
            System.out.println("return");
            return;
        }
        System.out.println("valid = " + true);
        if (!handle()) {
            System.out.println("error");
            sender.error("<mc:red>Unable to handle teleport request.");
        } else {
            System.out.println("ok");
        }

        System.out.println("timeoutToken = " + timeoutToken);
        TaskManager.INSTANCE.cancelTask(timeoutToken);
        System.out.println("valid = " + valid);
        this.valid = false;
        System.out.println("onAccept = " + onAccept);
        this.onAccept.forEach(Runnable::run);
        System.out.println("onFinalize = " + onFinalize);
        this.onFinalize.forEach(Runnable::run);
        System.out.println("id = " + id);
        TeleportManager.terminateRequest(id);
    }

    public void onAccept(Runnable func) {
        this.onAccept.add(func);
    }

    @ApiStatus.Internal
    public void cancel() {
        if (!valid) return;
        TaskManager.INSTANCE.cancelTask(timeoutToken);
        this.valid = false;
        this.onFinalize.forEach(Runnable::run);
    }

    public void onFinalize(Runnable func) {
        this.onFinalize.add(func);
    }

    public void startTimer() {
        ScheduledTask schedule = TaskManager.INSTANCE.schedule(this::timeOut, Config.getTeleportTimeout());
        this.timeoutToken = schedule.token();
    }

    public enum Type {
        INBOUND, OUTBOUND
    }
}
