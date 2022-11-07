package com.ultreon.mods.servercore.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/**
 * {@code /tphere} command and it's subcommands + arguments.
 *
 * @since 0.1.0
 */
public class TpHereCommand {
    private static LiteralCommandNode<CommandSourceStack> command;

    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        command = dispatcher.register(Commands.literal("tphere")
                .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.ask"))
                .then(Commands.argument("entity", EntityArgument.entity())
                        .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.ask"))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Player sender = source.getPlayerOrException();
                            Entity target = EntityArgument.getEntity(context, "entity");
                            target.teleportToWithTicket(sender.getX(), sender.getY(), sender.getZ());
                            return 1;
                        })
                )
        );
    }

    /**
     * Get the {@code /tphere} command root.
     *
     * @return the command node.
     */
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return command;
    }
}
