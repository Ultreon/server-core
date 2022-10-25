package com.ultreon.mods.servercore.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * {@code /gm} command and it's subcommands + arguments.
 *
 * @since 0.1.0
 */
public class GmCommand {
    private static LiteralCommandNode<CommandSourceStack> command;

    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        command = dispatcher.register(Commands.literal("gm")
                .redirect(dispatcher.getRoot().getChild("gamemode"))
        );
    }

    /**
     * Get the {@code /top} command root.
     *
     * @return the command node.
     */
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return command;
    }
}
