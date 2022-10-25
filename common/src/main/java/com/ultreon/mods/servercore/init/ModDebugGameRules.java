package com.ultreon.mods.servercore.init;

import net.minecraft.world.level.GameRules;

public class ModDebugGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> SERVER_CORE_DEBUG = GameRules.register("servercore:debug", GameRules.Category.MISC, GameRules.BooleanValue.create(false));

    public static void initNop() {

    }
}
