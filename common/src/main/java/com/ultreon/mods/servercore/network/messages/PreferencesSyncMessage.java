package com.ultreon.mods.servercore.network.messages;

import com.ultreon.mods.servercore.server.state.ServerStateManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

/**
 * Preference sync message.
 * Basically {@link StateSyncMessage} but opposite direction: Client to Server.
 *
 * @since 0.1.0
 */
public class PreferencesSyncMessage {
    private final ResourceLocation type;
    private final CompoundTag data;

    /**
     * Read the message data from the byte buffer.
     *
     * @param buf the byte buffer to read from.
     * @since 0.1.0
     */
    public PreferencesSyncMessage(FriendlyByteBuf buf) {
        this.type = buf.readResourceLocation();
        this.data = buf.readAnySizeNbt();
    }

    /**
     * Create the message from the data that will be sent.
     *
     * @param type type of data to sync.
     * @param data the data to sync/
     * @since 0.1.0
     */
    public PreferencesSyncMessage(ResourceLocation type, CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Encode the message to the byte buffer.
     *
     * @param buf the buffer to encode to.
     * @since 0.1.0
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(type);
        buf.writeNbt(data);
    }

    /**
     * Handle the message.
     *
     * @param contextSupplier context for where it originated from.
     */
    public void apply(Supplier<NetworkManager.PacketContext> contextSupplier) {
        NetworkManager.PacketContext context = contextSupplier.get();
        Player player = context.getPlayer();
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            manager.player(player).receive(type, data);
        }
    }
}