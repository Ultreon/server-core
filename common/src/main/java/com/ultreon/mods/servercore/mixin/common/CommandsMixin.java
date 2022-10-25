package com.ultreon.mods.servercore.mixin.common;

import com.ultreon.mods.servercore.init.ModDebugGameRules;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import dev.architectury.platform.Platform;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"), method = "performCommand")
    public boolean handleIsRunningInIdeFlag() {
        ServerStateManager manager = ServerStateManager.get();
        if (manager == null || !Platform.isDevelopmentEnvironment()) {
            return SharedConstants.IS_RUNNING_IN_IDE;
        }
        return SharedConstants.IS_RUNNING_IN_IDE || manager.server().getGameRules().getRule(ModDebugGameRules.SERVER_CORE_DEBUG).get();
    }
}
