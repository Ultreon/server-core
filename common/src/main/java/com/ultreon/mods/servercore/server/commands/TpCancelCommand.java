package com.ultreon.mods.servercore.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ultreon.mods.servercore.server.chat.ChatFormatter;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import com.ultreon.mods.servercore.server.teleport.PlayerTeleports;
import com.ultreon.mods.servercore.server.teleport.TeleportManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * {@code /tphere} command and it's subcommands + arguments.
 *
 * @since 0.1.0
 */
public class TpCancelCommand {
    private static LiteralCommandNode<CommandSourceStack> command;

    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        command = dispatcher.register(Commands.literal("tpcancel")
                .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.deny"))
                .then(Commands.argument("id", UuidArgument.uuid())
                        .requires(source -> Objects.requireNonNull(ServerStateManager.get()).hasPermission(source, "servercore.commands.tp.deny"))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Player sender = source.getPlayerOrException();
                            PlayerTeleports senderTp = TeleportManager.get().get(sender);
                            UUID id = UuidArgument.getUuid(context, "id");
                            if (!senderTp.cancelRequest(id)) {
                                ChatFormatter formatter = new ChatFormatter(TpHandleCommand.NOT_FOUND);
                                source.sendFailure(formatter.format().output());
                                return 0;
                            }
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
