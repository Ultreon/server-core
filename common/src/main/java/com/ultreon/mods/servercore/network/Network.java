package com.ultreon.mods.servercore.network;

import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.network.messages.PreferencesSyncMessage;
import com.ultreon.mods.servercore.network.messages.StateSyncMessage;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Network class for sending packets Server &lt;--&gt; Client.
 *
 * @since 0.1.0
 */
public class Network {
    /**
     * State sync packet ID.
     *
     * @since 0.1.0
     */
    public static final ResourceLocation STATE_SYNC_ID = ServerCore.res("state_sync");

    /**
     * Execute packet ID.
     *
     * @since 0.1.0
     */
    public static final ResourceLocation EXECUTE_ID = ServerCore.res("execute");

    /**
     * Network channel for the mod.
     *
     * @since 0.1.0
     */
    public static final NetworkChannel CHANNEL = NetworkChannel.create(ServerCore.res("net"));

    /**
     * Initialize the network system.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        CHANNEL.register(StateSyncMessage.class, StateSyncMessage::encode, StateSyncMessage::new, StateSyncMessage::apply);
        CHANNEL.register(PreferencesSyncMessage.class, PreferencesSyncMessage::encode, PreferencesSyncMessage::new, PreferencesSyncMessage::apply);
    }

    /**
     * Send a state synchronize packet.
     *
     * @param player the player to send it to.
     * @param type   the type of sync.
     * @param data   the data to sync.
     */
    public static void sendStateSync(ServerPlayer player, ResourceLocation type, CompoundTag data) {
        CHANNEL.sendToPlayer(player, new StateSyncMessage(type, data));
    }
}
