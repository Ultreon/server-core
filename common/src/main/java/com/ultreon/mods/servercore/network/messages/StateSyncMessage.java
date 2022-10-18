package com.ultreon.mods.servercore.network.messages;

import com.ultreon.mods.servercore.client.state.ClientStateManager;
import com.ultreon.mods.servercore.client.state.MultiplayerState;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * State synchronization message, synchronizes data from the server to the client.
 *
 * @since 0.1.0
 */
public class StateSyncMessage {
    private final ResourceLocation type;
    private final CompoundTag data;

    /**
     * Read the message data from the byte buffer.
     *
     * @param buf the byte buffer to read from.
     * @since 0.1.0
     */
    public StateSyncMessage(FriendlyByteBuf buf) {
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
    public StateSyncMessage(ResourceLocation type, CompoundTag data) {
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
        MultiplayerState multiplayer = ClientStateManager.get().getMultiplayer();
        if (multiplayer == null) {
            ClientStateManager.get().onJoin();
            multiplayer = ClientStateManager.get().getMultiplayer();
        }
        Objects.requireNonNull(multiplayer, "Multiplayer state is unloaded after manually loading while receiving multiplayer messages.");
        multiplayer.receive(type, data);
    }
}