package com.ultreon.mods.servercore.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Objects;

/**
 * {@code /servercore} command and it's subcommands + arguments.
 *
 * @since 0.1.0
 */
public class TopCommand {
    private static LiteralCommandNode<CommandSourceStack> command;

    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        command = dispatcher.register(Commands.literal("top")
                .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.top"))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    Entity entity = source.getEntity();
                    if (entity != null) {
                        Level level = entity.getLevel();
                        if (level == null) return 0;
                        int x = entity.getBlockX();
                        int z = entity.getBlockZ();
                        double height = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                        entity.teleportToWithTicket(x + 0.5, height, z + 0.5);
                        return 1;
                    }
                    return 0;
                })
        );
    }

    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return command;
    }
}
