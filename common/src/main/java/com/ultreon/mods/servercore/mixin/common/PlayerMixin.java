package com.ultreon.mods.servercore.mixin.common;

import com.ultreon.mods.servercore.server.chat.ChatContext;
import com.ultreon.mods.servercore.server.chat.ChatFormatter;
import com.ultreon.mods.servercore.server.teleport.*;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings("ConstantConditions")
@Mixin(Entity.class)
public abstract class PlayerMixin implements PlayerTeleports {
    @Shadow public abstract UUID getUUID();

    @Shadow public abstract void sendSystemMessage(Component component);

    @Shadow public abstract void teleportToWithTicket(double d, double e, double f);


    @Shadow public abstract void remove(Entity.RemovalReason removalReason);

    @Shadow public abstract void setPos(Vec3 vec3);

    private static final String INBOUND_SENT = """
            <mc:green>Request to teleport you to </>%recipient-name%<mc:green> was sent.
            <mc:green>Options: {click:/tpcancel %teleport-id%}<mc:dark-red>[<mc:red>__CANCEL__<mc:dark-red>]</>""";
    private static final String OUTBOUND_SENT = """
            <mc:green>Request to teleport </>%recipient-name%<mc:green> to you was sent.
            <mc:green>Options: {click:/tpcancel %teleport-id%}<mc:dark-red>[<mc:red>__CANCEL__<mc:dark-red>]</>""";

    private static final String INBOUND_RECEIVE = """
            <mc:blue>Inbound Teleport: </>%sender-name%<mc:gold> wants to teleport to you.
            <mc:gold>Options: {click:/tpaccept %teleport-id%}<mc:dark-green>[<mc:green>__ACCEPT__<mc:dark-green>]</> {click:/tpdeny %teleport-id%}<mc:dark-red>[<mc:red>__DENY__<mc:dark-red>]</>""";
    private static final String OUTBOUND_RECEIVE = """
            <mc:blue>Outbound Teleport: </>%sender-name%<mc:gold> wants you teleport to them.
            <mc:gold>Options: {click:/tpaccept %teleport-id%}<mc:dark-green>[<mc:green>__ACCEPT__<mc:dark-green>]</> {click:/tpdeny %teleport-id%}<mc:dark-red>[<mc:red>__DENY__<mc:dark-red>]</>""";

    private static final String CANCELLED_BY_SENDER = """
            <mc:gold></>%sender-name%<mc:gold> has cancelled their teleport request.""";

    private static final String CANCEL_SUCCESS = """
            <mc:gold>Successfully cancelled request sent to </>%recipient-name%<mc:gold>.""";

    private static final String TIMED_OUT = """
            <mc:gold>Request that </>%sender-name%<mc:gold> sent has <mc:aqua>timed out<mc:gold>.""";
    private static final String GOT_TIMED_OUT = """
            <mc:gold>The teleport request sent to </>%recipient-name%<mc:gold> has <mc:aqua>timed out<mc:gold>.""";

    private static final String DENIED = """
            <mc:gold>You <mc:red>denied<mc:gold> the request of </>%sender-name%<mc:gold> sent.""";
    private static final String GOT_DENIED = """
            <mc:gold>The teleport request sent to </>%recipient-name%<mc:gold> got <mc:red>denied<mc:gold>.""";

    private static final String ACCEPTED = """
            <mc:gold>You <mc:green>accepted<mc:gold> the request that </>%sender-name%<mc:gold> sent.""";
    private static final String GOT_ACCEPTED = """
            <mc:gold>The teleport request sent to </>%recipient-name%<mc:gold> got <mc:green>accepted<mc:gold>.""";

    private static final Set<UUID> ACTIVE_TELEPORTS = new CopyOnWriteArraySet<>();
    private final Object lock = new Object();

    // Direct teleports
    private Teleport<?> currentTeleport = null;
    private UUID currentTeleportId;

    // Teleport requests.
    private final Map<UUID, TeleportRequest> sentRequests = new HashMap<>();
    private final Map<UUID, TeleportRequest> receivedRequests = new HashMap<>();

    @Override
    public UUID getId() {
        return getUUID();
    }

    @Override
    public void requestTeleportTo(RequestRecipient<?> recipient) {
        TeleportRequest request = TeleportRequest.inbound(TeleportManager.nextReqId(), recipient, this);
        request.onTimeOut(() -> timeOut(request));
        request.onDeny(() -> doDeny(request));
        request.onAccept(() -> doAccept(request));

        recipient.receiveInbound(request);

        sentRequests.put(request.id(), request);

        TeleportManager.registerRequest(request);
        request.startTimer();

        ChatFormatter formatter = new ChatFormatter(INBOUND_SENT, request.getMessageContext());
        sendSystemMessage(formatter.format().output());
    }

    @Override
    public void requestTeleportFrom(RequestRecipient<?> recipient) throws IllegalRecipientException {
        if (recipient.isStatic()) throw new IllegalRecipientException("Can't request to teleport a static recipient.");

        TeleportRequest request = TeleportRequest.outbound(TeleportManager.nextReqId(), recipient, this);
        request.onTimeOut(() -> timeOut(request));
        request.onDeny(() -> doDeny(request));
        request.onAccept(() -> doAccept(request));

        recipient.receiveOutbound(request);

        sentRequests.put(request.id(), request);

        TeleportManager.registerRequest(request);
        request.startTimer();

        ChatFormatter formatter = new ChatFormatter(OUTBOUND_SENT, request.getMessageContext());
        sendSystemMessage(formatter.format().output());
    }

    private void timeOut(TeleportRequest request) {
        ChatContext messageContext = request.getMessageContext();
        {
            ChatFormatter formatter = new ChatFormatter(GOT_TIMED_OUT, messageContext);
            sendSystemMessage(formatter.format().output());
        }

        if (request instanceof EntityRecipient<?> r) {
            ChatFormatter formatter = new ChatFormatter(TIMED_OUT, messageContext);
            Entity recipient = r.recipient();
            if (recipient != null) {
                recipient.sendSystemMessage(formatter.format().output());
            }
        }
    }

    private void doDeny(TeleportRequest request) {
        ChatContext messageContext = request.getMessageContext();
        {
            ChatFormatter formatter = new ChatFormatter(GOT_DENIED, messageContext);
            sendSystemMessage(formatter.format().output());
        }

        if (request.recipient() instanceof EntityRecipient<?> r) {
            ChatFormatter formatter = new ChatFormatter(DENIED, messageContext);
            Entity recipient = r.recipient();
            if (recipient != null) {
                recipient.sendSystemMessage(formatter.format().output());
            }
        }
    }

    private void doAccept(TeleportRequest request) {
        ChatContext messageContext = request.getMessageContext();
        {
            ChatFormatter formatter = new ChatFormatter(GOT_ACCEPTED, messageContext);
            sendSystemMessage(formatter.format().output());
        }

        if (request.recipient() instanceof EntityRecipient<?> r) {
            ChatFormatter formatter = new ChatFormatter(ACCEPTED, messageContext);
            Entity recipient = r.recipient();
            if (recipient != null) {
                recipient.sendSystemMessage(formatter.format().output());
            }
        }
    }

    @Override
    public void receiveInbound(TeleportRequest request) {
        if (request.recipient() instanceof EntityRecipient<?> r) {
            Entity recipient = r.recipient();
            Objects.requireNonNull(recipient);
            if (!Objects.equals(recipient, entity())) request.sender().error("<mc:red>Sent request was not received by the correct entity.");
            ChatFormatter formatter = new ChatFormatter(INBOUND_RECEIVE, new ChatContext()
                    .key("recipient-name", this.getObjName().getString())
                    .key("sender-name", request.sender().getObjName().getString())
                    .key("teleport-id", request.id()));
            sendSystemMessage(formatter.format().output());
        }

        receivedRequests.put(request.id(), request);
    }

    @Override
    public void receiveOutbound(TeleportRequest request) {
        if (request.recipient() instanceof EntityRecipient<?> r) {
            Entity recipient = r.recipient();
            if (!Objects.equals(recipient, entity())) request.sender().error("<mc:red>Sent request was not received by the correct entity.");
            Objects.requireNonNull(recipient);
            ChatFormatter formatter = new ChatFormatter(OUTBOUND_RECEIVE, new ChatContext()
                    .key("recipient-name", this.getObjName().getString())
                    .key("sender-name", request.sender().getObjName().getString())
                    .key("teleport-id", request.id()));
            sendSystemMessage(formatter.format().output());
        }

        receivedRequests.put(request.id(), request);
    }

    @Override
    public void handleSentRequest(TeleportRequest request, TeleportRequest.Type type) {
        sentRequests.remove(request.id());
    }

    @Override
    public boolean cancelRequest(UUID request) {
        if (!sentRequests.containsKey(request)) return false;
        TeleportRequest removed = sentRequests.remove(request);
        removed.cancel();

        {
            ChatFormatter formatter = new ChatFormatter(CANCEL_SUCCESS, removed.getMessageContext());
            sendSystemMessage(formatter.format().output());
        }

        if (removed.recipient() instanceof EntityRecipient<?> receiver) {
            Entity entity = receiver.recipient();
            Objects.requireNonNull(entity);
            ChatFormatter formatter = new ChatFormatter(CANCELLED_BY_SENDER, removed.getMessageContext());
            entity.sendSystemMessage(formatter.format().output());
        }

        return true;
    }

    @Override
    public boolean denyRequest(UUID request) {
        if (!receivedRequests.containsKey(request)) return false;
        TeleportRequest removed = receivedRequests.remove(request);
        removed.deny();

        return true;
    }

    @Override
    public boolean acceptRequest(UUID request) {
        if (!receivedRequests.containsKey(request)) return false;
        TeleportRequest removed = receivedRequests.remove(request);
        removed.accept();

        return true;
    }

    @Override
    public void teleportTo(Entity destination) {
        if (currentTeleport != null) return;

        UUID uuid;
        synchronized (lock) {
            do {
                uuid = UUID.randomUUID();
            } while (ACTIVE_TELEPORTS.contains(uuid));

            ACTIVE_TELEPORTS.add(uuid);

            currentTeleport = new Teleport<>(getUUID(), TeleportDestination.entity(destination));
            currentTeleport.onSuccess(this::clearTp);
            currentTeleport.onFail(this::clearTp);
            currentTeleportId = uuid;
            currentTeleport.prepare();
        }
    }

    @Override
    public synchronized void teleportTo(Vec3 destination) {
        if (currentTeleport != null) return;

        UUID uuid;
        synchronized (lock) {
            do {
                uuid = UUID.randomUUID();
            } while (ACTIVE_TELEPORTS.contains(uuid));

            ACTIVE_TELEPORTS.add(uuid);

            currentTeleport = new Teleport<>(getUUID(), TeleportDestination.pos(destination));
            currentTeleport.onSuccess(this::clearTp);
            currentTeleport.onFail(this::clearTp);
            currentTeleportId = uuid;
            currentTeleport.prepare();
        }
    }

    @Override
    public synchronized void teleportTo(RequestSender destination) {
        teleportTo(destination.getObjPos());
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public void error(String message) {
        ChatFormatter formatter = new ChatFormatter(message);
        entity().sendSystemMessage(formatter.format().output());
    }

    private void clearTp() {
        synchronized (lock) {
            ACTIVE_TELEPORTS.remove(currentTeleportId);

            currentTeleport = null;
            currentTeleportId = null;
        }
    }

    @Override
    public boolean isOnline() {
        return player() != null;
    }

    @Override
    public Component getObjName() {
        return entity().getName();
    }

    @Override
    public void teleportTo(RequestRecipient<?> recipient) {
        System.out.println("recipient = " + recipient);
        Vec3 pos = recipient.getObjPos();
        System.out.println("pos = " + pos);
        teleportToWithTicket(pos.x, pos.y, pos.z);
        System.out.println("pos = " + pos);
    }

    public Entity entity() {
        return (Entity)(Object) this;
    }

    public Player player() {
        return (Player)(Object)this;
    }

    @Override
    public Player recipient() {
        return player();
    }

    @Override
    public void cancelTp() {
        if (currentTeleport == null) {
            return;
        }
        currentTeleport.cancel();
        clearTp();
    }
    
    @Override
    public void onLeft() {
        clearTp();
        sentRequests.values().forEach(TeleportRequest::deny);
    }

    @Override
    public Vec3 getObjPos() {
        Entity recipient = this.recipient();
        System.out.println("recipient = " + recipient);
        return recipient != null ? recipient.position() : null;
    }
}
