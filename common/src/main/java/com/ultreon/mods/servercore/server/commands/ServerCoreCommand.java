package com.ultreon.mods.servercore.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ultreon.mods.servercore.common.ThrowConsumer;
import com.ultreon.mods.servercore.server.DefaultRank;
import com.ultreon.mods.servercore.server.Permission;
import com.ultreon.mods.servercore.server.Rank;
import com.ultreon.mods.servercore.server.chat.ChatContext;
import com.ultreon.mods.servercore.server.chat.ChatFormatter;
import com.ultreon.mods.servercore.server.state.ServerPlayerState;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;

/**
 * {@code /servercore} command and it's subcommands + arguments.
 *
 * @since 0.1.0
 */
public class ServerCoreCommand {
    private static LiteralCommandNode<CommandSourceStack> command;

    /**
     * Register the command using the command dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        command = dispatcher.register(Commands.literal("servercore")
                .then(Commands.literal("cmd")
                        .then(Commands.literal("top").fork(TopCommand.getCommand(), context -> List.of(context.getSource())))
                        .then(Commands.literal("gm").fork(GmCommand.getCommand(), context -> List.of(context.getSource())))
                        .then(Commands.literal("sudo").fork(SudoCommand.getCommand(), context -> List.of(context.getSource())))
                ).then(Commands.literal("user")
                        .then(Commands.argument("user", GameProfileArgument.gameProfile())
                                .then(Commands.literal("perms")
                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.user.permissions"))
                                        .then(Commands.literal("add")
                                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.user.permissions.edit"))
                                                .then(Commands.argument("permission", StringArgumentType.string())
                                                        .suggests((context, builder) -> {
                                                            ServerStateManager manager = ServerStateManager.get();
                                                            if (manager == null) return builder.buildFuture();
                                                            for (Permission permission : manager.getGlobalPermissions()) {
                                                                builder.suggest(permission.id());
                                                            }
                                                            return builder.buildFuture();
                                                        }).executes(ServerCoreCommand::addPermission)
                                                )
                                        ).then(Commands.literal("remove")
                                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.user.permissions.edit"))
                                                .then(Commands.argument("permission", StringArgumentType.string())
                                                        .suggests((context, builder) -> {
                                                            ServerStateManager manager = ServerStateManager.get();
                                                            if (manager == null) return builder.buildFuture();
                                                            for (Permission permission : manager.getGlobalPermissions()) {
                                                                builder.suggest(permission.id());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ServerCoreCommand::removePermission)
                                                )
                                        )
                                ).then(Commands.literal("ranks")
                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.user.ranks"))
                                        .then(Commands.literal("add")
                                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.user.ranks.add"))
                                                .then(Commands.argument("rank", StringArgumentType.string())
                                                        .suggests((context, builder) -> {
                                                            ServerStateManager manager = ServerStateManager.get();
                                                            if (manager == null) return builder.buildFuture();
                                                            for (Rank rank : manager.getRanks()) {
                                                                if (rank instanceof DefaultRank) continue;
                                                                builder.suggest(rank.getId());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ServerCoreCommand::addRank)
                                                )
                                        ).then(Commands.literal("remove")
                                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.user.ranks.remove"))
                                                .then(Commands.argument("rank", StringArgumentType.string())
                                                        .suggests((context, builder) -> {
                                                            ServerStateManager manager = ServerStateManager.get();
                                                            if (manager == null) return builder.buildFuture();
                                                            for (Rank rank : manager.getRanks()) {
                                                                if (rank instanceof DefaultRank) continue;
                                                                builder.suggest(rank.getId());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ServerCoreCommand::removeRank)
                                                )
                                        )
                                )
                        )
                ).then(Commands.literal("ranks")
                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks"))
                        .then(Commands.literal("edit")
                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit"))
                                .then(Commands.argument("rank", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            ServerStateManager manager = ServerStateManager.get();
                                            if (manager == null) return builder.buildFuture();
                                            for (Rank rank : manager.getRanks()) {
                                                if (rank instanceof DefaultRank) continue;
                                                builder.suggest(rank.getId());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.literal("perms")
                                                .then(Commands.literal("add")
                                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit.permissions"))
                                                        .then(Commands.argument("permission", StringArgumentType.string())
                                                                .suggests((context, builder) -> {
                                                                    ServerStateManager manager = ServerStateManager.get();
                                                                    if (manager == null) return builder.buildFuture();
                                                                    for (Permission permission : manager.getGlobalPermissions()) {
                                                                        builder.suggest(permission.id());
                                                                    }
                                                                    return builder.buildFuture();
                                                                }).executes(ServerCoreCommand::addPermissionToRank)
                                                        )
                                                ).then(Commands.literal("remove")
                                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit.permissions"))
                                                        .then(Commands.argument("permission", StringArgumentType.string())
                                                                .suggests((context, builder) -> {
                                                                    ServerStateManager manager = ServerStateManager.get();
                                                                    if (manager == null) return builder.buildFuture();
                                                                    for (Permission permission : manager.getGlobalPermissions()) {
                                                                        builder.suggest(permission.id());
                                                                    }
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(ServerCoreCommand::removePermissionFromRank)
                                                        )
                                                )
                                        ).then(Commands.literal("set")
                                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit.properties"))
                                                .then(Commands.literal("getObjName")
                                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit.properties.getObjName"))
                                                        .then(Commands.argument("value", StringArgumentType.string())
                                                                .executes(ServerCoreCommand::setNameOfRank)
                                                        )
                                                )
                                                .then(Commands.literal("prefix")
                                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit.properties.prefix"))
                                                        .then(Commands.argument("value", StringArgumentType.string())
                                                                .executes(ServerCoreCommand::setPrefixOfRank)
                                                        )
                                                )
                                                .then(Commands.literal("priority")
                                                        .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.edit.properties.priority"))
                                                        .then(Commands.argument("value", IntegerArgumentType.integer())
                                                                .executes(ServerCoreCommand::setPriorityOfRank)
                                                        )
                                                )
                                        )
                                )
                        ).then(Commands.literal("create")
                                .requires(commandSourceStack -> ServerStateManager.get() != null && ServerStateManager.get().hasPermission(commandSourceStack, "servercore.ranks.create"))
                                .then(Commands.argument("id", StringArgumentType.string())
                                        .then(Commands.argument("getObjName", StringArgumentType.string())
                                                .then(Commands.argument("prefix", StringArgumentType.string())
                                                        .then(Commands.argument("priority", IntegerArgumentType.integer())
                                                                .executes(ServerCoreCommand::createRank)
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setNameOfRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String rankId = StringArgumentType.getString(context, "rank");
        String name = StringArgumentType.getString(context, "value");
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null && manager.hasRank(rankId)) {
            manager.getRank(rankId).setName(name);
            context.getSource().sendSuccess(Component.translatable("command.servercore.ranks.name_set", rankId, name), true);
            return 1;
        } else {
            throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
        }
    }

    private static int setPrefixOfRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String rankId = StringArgumentType.getString(context, "rank");
        String prefix = StringArgumentType.getString(context, "value");
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null && manager.hasRank(rankId)) {
            ChatFormatter formatter = new ChatFormatter(prefix, new ChatContext(), false, true);
            ChatFormatter.Results prefixFormatted = formatter.format();
            manager.getRank(rankId).setPrefix(prefix);
            context.getSource().sendSuccess(Component.translatable("command.servercore.ranks.prefix_set", rankId, prefixFormatted.output()), true);
            return 1;
        } else {
            throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
        }
    }

    private static int setPriorityOfRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String rankId = StringArgumentType.getString(context, "rank");
        int priority = IntegerArgumentType.getInteger(context, "value");
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null && manager.hasRank(rankId)) {
            if (manager.getRank(rankId) instanceof DefaultRank) {
                throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.unable_to_edit", rankId)).create();
            }
            manager.getRank(rankId).setPriority(priority);
            context.getSource().sendSuccess(Component.translatable("command.servercore.ranks.priority_set", rankId, priority), true);
            return 1;
        } else {
            throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
        }
    }

    private static int createRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "id");
        String name = StringArgumentType.getString(context, "getObjName");
        String prefix = StringArgumentType.getString(context, "prefix");
        int priority = IntegerArgumentType.getInteger(context, "priority");

        ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            if (manager.getRank(id) instanceof DefaultRank) {
                throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.unable_to_create", id)).create();
            }
            manager.addRank(id, name, prefix, priority);
            context.getSource().sendSuccess(Component.translatable("command.servercore.ranks.created", id), true);
            return 1;
        } else {
            throw new SimpleCommandExceptionType(Component.translatable("command.servercore._.manager_offline")).create();
        }
    }

    private static int addPermissionToRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String rankId = StringArgumentType.getString(context, "rank");
        String permissionId = StringArgumentType.getString(context, "permission");
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null && manager.hasRank(rankId)) {
            manager.getRank(rankId).addPermission(new Permission(permissionId));
            return 1;
        } else {
            throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
        }
    }

    private static int removePermissionFromRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String rankId = StringArgumentType.getString(context, "rank");
        String permissionId = StringArgumentType.getString(context, "permission");
        ServerStateManager manager = ServerStateManager.get();
        if (manager != null && manager.hasRank(rankId)) {
            manager.getRank(rankId).removePermission(new Permission(permissionId));
            return 1;
        } else {
            throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
        }
    }

    private static int addRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeUser(context, state -> {
            String rankId = StringArgumentType.getString(context, "rank");
            ServerStateManager manager = ServerStateManager.get();
            if (manager != null && manager.hasRank(rankId)) {
                if (manager.getRank(rankId) instanceof DefaultRank) {
                    throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.unable_to_add", rankId)).create();
                }
                state.addRank(rankId);
            } else {
                throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
            }
        });
    }

    private static int removeRank(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeUser(context, state -> {
            String rankId = StringArgumentType.getString(context, "rank");
            ServerStateManager manager = ServerStateManager.get();
            if (manager != null && manager.hasRank(rankId)) {
                if (manager.getRank(rankId) instanceof DefaultRank) {
                    throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.unable_to_remove", rankId)).create();
                }
                state.removeRank(rankId);
            } else {
                throw new SimpleCommandExceptionType(Component.translatable("command.servercore.ranks.not_found", rankId)).create();
            }
        });
    }

    /**
     * Get the command.
     *
     * @return the command.
     * @since 0.1.0
     */
    public static LiteralCommandNode<CommandSourceStack> getCommand() {
        return command;
    }

    private static int addPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeUser(context, state -> {
            String permissionId = StringArgumentType.getString(context, "permission");
            state.addPermission(new Permission(permissionId));
        });
    }

    private static int removePermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeUser(context, state -> {
            String permissionId = StringArgumentType.getString(context, "permission");
            state.removePermission(new Permission(permissionId));
        });
    }

    private static int executeUser(CommandContext<CommandSourceStack> context, ThrowConsumer<ServerPlayerState, CommandSyntaxException> func) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "user");
        boolean executed = false;

        for (GameProfile profile : profiles) {
            if (ServerStateManager.get() != null) {
                ServerPlayerState state = ServerStateManager.get().player(profile);

                if (state == null) {
                    source.sendFailure(Component.translatable("commands.servercore._.user_not_found"));
                    return 0;
                }

                func.accept(state);
                executed = true;
            } else {
                throw new CommandRuntimeException(Component.literal("Server is offline. How did this even happen?"));
            }
        }

        return executed ? 1 : 0;
    }
}
