package com.ultreon.mods.servercore.server;

import com.mojang.brigadier.CommandDispatcher;
import com.ultreon.mods.servercore.server.commands.PermissionCommand;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Server events class.
 * Handles server-side events.
 *
 * @since 0.1.0
 */
public class ServerEvents {
    private static ServerEvents instance;
    private final ReferenceArraySet<Player> hasTicked = new ReferenceArraySet<>();

    private ServerEvents() {
        LifecycleEvent.SERVER_BEFORE_START.register(this::start);
        LifecycleEvent.SERVER_STOPPED.register(this::stop);

        TickEvent.PLAYER_POST.register(this::onPlayerTick);
        PlayerEvent.PLAYER_QUIT.register(this::onQuit);

        CommandRegistrationEvent.EVENT.register(this::registerCommands);
    }

    private void onPlayerTick(Player player) {
        if (!hasTicked.contains(player) && player instanceof ServerPlayer p) {
            onJoin(p);
            hasTicked.add(player);
        }
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        PermissionCommand.register(dispatcher);
    }

    private EventResult onAddEntity(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            this.onJoin(player);
        }
        return EventResult.pass();
    }

    private void onJoin(ServerPlayer player) {
        final ServerStateManager manager = ServerStateManager.get();
        Objects.requireNonNull(manager, "Server state manager is unloaded while a player joined.");
        manager.player(player).onJoin(player);
    }

    private void onQuit(ServerPlayer player) {
        final ServerStateManager manager = ServerStateManager.get();
        if (manager != null) {
            manager.player(player).onQuit();
        }
        hasTicked.remove(player);
    }

    /**
     * Get the instance of the server events class.
     *
     * @return the instance.
     * @since 0.1.0
     */
    public static ServerEvents get() {
        return instance;
    }

    private void start(MinecraftServer server) {
        ServerStateManager.start(server);
    }

    private void stop(MinecraftServer server) {
        ServerStateManager.stop();
    }

    /**
     * Initialize the server events handlers.
     *
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public static void init() {
        instance = new ServerEvents();
    }
}
