package com.ultreon.mods.servercore.mixin.common;

import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * Minecraft Server mixin.
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    /**
     * Saving injection.
     *
     * @param bl  arg 1.
     * @param bl2 arg 2.
     * @param bl3 arg 3.
     * @param cir callback info.
     * @since 0.1.0
     */
    @ApiStatus.Internal
    @Inject(at = @At("RETURN"), method = "saveEverything")
    public void saveEverything(boolean bl, boolean bl2, boolean bl3, CallbackInfoReturnable<Boolean> cir) {
        ServerStateManager manager = ServerStateManager.get();
        Objects.requireNonNull(manager, "Server state manager is unloaded when saving local data.");
        manager.saveLocal();
    }
}
