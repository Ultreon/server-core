package com.ultreon.mods.servercore.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import com.ultreon.mods.servercore.server.teleport.EntityRecipient;
import com.ultreon.mods.servercore.server.teleport.IllegalRecipientException;
import com.ultreon.mods.servercore.server.teleport.PlayerTeleports;
import com.ultreon.mods.servercore.server.teleport.TeleportManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/**
 * {@code /tpa} and {@code /tpahere} commands and their subcommands + arguments.
 *
 * @since 0.1.0
 */
public class TpAskCommand {
    private static LiteralCommandNode<CommandSourceStack> command;
    private static LiteralCommandNode<CommandSourceStack> hereCommand;

    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        command = dispatcher.register(Commands.literal("tpa")
                .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.ask"))
                .then(Commands.argument("entity", EntityArgument.entity())
                        .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.ask"))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Player sender = source.getPlayerOrException();
                            PlayerTeleports senderTp = TeleportManager.get().get(sender);
                            Entity destination = EntityArgument.getEntity(context, "entity");
                            if (destination instanceof EntityRecipient<?> recipient) {
                                senderTp.requestTeleportTo(recipient);
                                return 1;
                            }
                            return 0;
                        })
                )
        );
        hereCommand = dispatcher.register(Commands.literal("tpahere")
                .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.ask"))
                .then(Commands.argument("entity", EntityArgument.entity())
                        .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.ask"))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Player sender = source.getPlayerOrException();
                            PlayerTeleports senderTp = TeleportManager.get().get(sender);
                            Entity destination = EntityArgument.getEntity(context, "entity");
                            if (destination instanceof EntityRecipient<?> recipient) {
                                try {
                                    senderTp.requestTeleportFrom(recipient);
                                } catch (IllegalRecipientException e) {
                                    source.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
                                    return 0;
                                }
                                return 1;
                            }
                            return 0;
                        })
                )
        );
    }

    /**
     * Get the {@code /tpa} command root.
     *
     * @return the command node.
     */
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return command;
    }

    /**
     * Get the {@code /tpahere} command root.
     *
     * @return the command node.
     */
    public static LiteralCommandNode<CommandSourceStack> getHereCommand() {
        return hereCommand;
    }
}
