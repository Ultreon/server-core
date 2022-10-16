package com.ultreon.mods.servercore.network;

import com.ultreon.mods.servercore.ServerCore;
import com.ultreon.mods.servercore.client.state.ClientStateManager;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import static dev.architectury.networking.NetworkManager.*;

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
     * Initialize the network system.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        // We are using S2C here for an example, use C2S instead if this is from the client to the server
        registerReceiver(Side.S2C, STATE_SYNC_ID, (buf, context) -> {
            Player player = context.getPlayer();
            ClientStateManager.get().getMultiplayer().receive(buf.readResourceLocation(), buf.readAnySizeNbt());
        });
        registerReceiver(Side.C2S, EXECUTE_ID, (buf, context) -> {
            Player player = context.getPlayer();
            ServerStateManager.get().player(player).receive(buf.readResourceLocation(), buf.readAnySizeNbt());
        });
    }

    /**
     * Send a state synchronize packet.
     *
     * @param type the type of sync.
     * @param data the data to sync.
     */
    public static void sendStateSync(ResourceLocation type, CompoundTag data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(type);
        buf.writeNbt(data);
        sendToServer(STATE_SYNC_ID, buf);
    }
}
