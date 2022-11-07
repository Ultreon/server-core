package com.ultreon.mods.servercore.mixin.common;

import com.ultreon.mods.servercore.client.state.ClientStateManager;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keyboard mixin class.
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    /**
     * Input schedule injection.
     * @param bl arg 1
     * @param f  arg 2
     * @param ci callback info.
     */
    @ApiStatus.Internal
    @Inject(at = @At("RETURN"), method = "tick")
    public void tick(boolean bl, float f, CallbackInfo ci) {
        if (!ClientStateManager.get().getMultiplayer().canWalk()) {
            this.up = false;
            this.down = false;
            this.left = false;
            this.right = false;
            this.forwardImpulse = 0f;
            this.leftImpulse = 0f;
        }
        if (!ClientStateManager.get().getMultiplayer().canJump()) {
            this.jumping = false;
        }
    }
}
