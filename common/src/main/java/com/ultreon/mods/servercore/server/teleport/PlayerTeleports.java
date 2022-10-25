package com.ultreon.mods.servercore.server.teleport;

import com.ultreon.mods.servercore.server.chat.ChatContext;
import com.ultreon.mods.servercore.server.chat.ChatFormatter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTeleports {
    private final TeleportManager manager;
    private final UUID id;
    private final Map<UUID, Teleport> teleports = new HashMap<>();
    private final Map<UUID, TeleportRequest> teleportsReqs = new HashMap<>();
    private UUID uuid;

    public PlayerTeleports(TeleportManager manager, UUID id) {
        this.manager = manager;
        this.id = id;
    }

    public TeleportManager getManager() {
        return manager;
    }

    public UUID getId() {
        return id;
    }

    public void requestTeleportTo(UUID uuid) {
        PlayerTeleports destination = manager.get(uuid);
        TeleportRequest request = TeleportRequest.inbound(uuid, this);

        destination.receiveInbound(request);
    }

    private void receiveInbound(TeleportRequest request) {
        if (request instanceof EntityReceiver<?> receiver) {
            Entity entity = receiver.getReceiver();
            ChatFormatter formatter = new ChatFormatter("", new ChatContext()
                    .key("receiver-name", entity.getName().getString())
                    .key("requester-name", request.requester.getEntity().getName().getString()));
            entity.sendSystemMessage(formatter.format().output());
        }
    }

    private Entity getEntity() {
        return getPlayer();
    }

    private Player getPlayer() {
        return manager.getServer().getPlayerList().getPlayer(uuid);
    }

    public void teleportTo(Entity entity) {
        ServerPlayer player = manager.getServer().getPlayerList().getPlayer(uuid);
        if (player == null) return;

        player.teleportToWithTicket(entity.getX(), entity.getY(), entity.getZ());
    }
}
