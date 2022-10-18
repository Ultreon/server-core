package com.ultreon.mods.servercore.mixin.common;

import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * Player List mixin.
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    /**
     * Singular player saving injection.
     *
     * @param serverPlayer the player saved.
     * @param ci           callback info.
     */
    @ApiStatus.Internal
    @Inject(at = @At("RETURN"), method = "save")
    public void save(ServerPlayer serverPlayer, CallbackInfo ci) {
        ServerStateManager manager = ServerStateManager.get();
        Objects.requireNonNull(manager, "Server state manager is unloaded when saving player: " + serverPlayer.getUUID());
        manager.player(serverPlayer).save();
    }
}
