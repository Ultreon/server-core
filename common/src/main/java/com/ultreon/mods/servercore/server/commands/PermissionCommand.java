package com.ultreon.mods.servercore.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultreon.mods.servercore.server.Permission;
import com.ultreon.mods.servercore.server.state.ServerPlayerState;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /permission} command and it's subcommands + arguments.
 *
 * @since 0.1.0
 */
public class PermissionCommand {
    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("permissions")
                .requires(commandSourceStack -> {
                    if (ServerStateManager.get() != null) {
                        return ServerStateManager.get().hasPermission(commandSourceStack, "servercore.permissions.access")
                                || ServerStateManager.get().hasPermission(commandSourceStack, "servercore.permissions.edit");
                    }
                    return false;
                })
                .then(Commands.literal("add")
                        .then(Commands.argument("permission", StringArgumentType.string())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    String permissionId = StringArgumentType.getString(context, "permission");

                                    ServerPlayer player = source.getPlayerOrException();
                                    ServerPlayerState state = null;
                                    if (ServerStateManager.get() != null) {
                                        state = ServerStateManager.get().player(player);
                                        state.addPermission(new Permission(permissionId));
                                    } else {
                                        throw new CommandRuntimeException(Component.literal("Server is offline. How did this even happen?"));
                                    }
                                    return 0;
                                })
                        )
                )
        );
    }
}
