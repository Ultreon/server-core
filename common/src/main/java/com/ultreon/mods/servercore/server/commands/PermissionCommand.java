package com.ultreon.mods.servercore.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultreon.mods.servercore.server.Permission;
import com.ultreon.mods.servercore.server.state.ServerPlayerState;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;

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
        dispatcher.register(Commands.literal("servercore")
                .then(Commands.literal("user")
                        .then(Commands.argument("user", GameProfileArgument.gameProfile())
                                .then(Commands.literal("perms")
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
                                                            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "user");
                                                            boolean permissionsSet = false;

                                                            for (GameProfile profile : profiles) {
                                                                if (ServerStateManager.get() != null) {
                                                                    ServerPlayerState state = ServerStateManager.get().player(profile);

                                                                    if (state == null) {
                                                                        source.sendFailure(Component.translatable("commands.servercore._.user_not_found"));
                                                                        return 0;
                                                                    }

                                                                    state.addPermission(new Permission(permissionId));
                                                                    permissionsSet = true;
                                                                } else {
                                                                    throw new CommandRuntimeException(Component.literal("Server is offline. How did this even happen?"));
                                                                }
                                                            }

                                                            return permissionsSet ? 1 : 0;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("permission", StringArgumentType.string())
                                                        .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            String permissionId = StringArgumentType.getString(context, "permission");
                                                            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "user");
                                                            boolean permissionsSet = false;

                                                            for (GameProfile profile : profiles) {
                                                                if (ServerStateManager.get() != null) {
                                                                    ServerPlayerState state = ServerStateManager.get().player(profile);

                                                                    if (state == null) {
                                                                        source.sendFailure(Component.translatable("commands.servercore._.user_not_found"));
                                                                        return 0;
                                                                    }

                                                                    state.removePermission(new Permission(permissionId));
                                                                    permissionsSet = true;
                                                                } else {
                                                                    throw new CommandRuntimeException(Component.literal("Server is offline. How did this even happen?"));
                                                                }
                                                            }

                                                            return permissionsSet ? 1 : 0;
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
