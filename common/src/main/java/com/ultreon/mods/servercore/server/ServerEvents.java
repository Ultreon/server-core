package com.ultreon.mods.servercore.server;

import com.mojang.brigadier.CommandDispatcher;
import com.ultreon.mods.servercore.server.chat.ChatContext;
import com.ultreon.mods.servercore.server.chat.ChatFormatter;
import com.ultreon.mods.servercore.server.commands.GmCommand;
import com.ultreon.mods.servercore.server.commands.ServerCoreCommand;
import com.ultreon.mods.servercore.server.commands.SudoCommand;
import com.ultreon.mods.servercore.server.commands.TopCommand;
import com.ultreon.mods.servercore.server.event.ChatContextEvent;
import com.ultreon.mods.servercore.server.state.ServerStateManager;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
        PlayerEvent.PLAYER_QUIT.register(this::onQuit);

        ChatEvent.DECORATE.register(this::decorateChat);
        ChatEvent.RECEIVED.register(this::receiveChat);

        CommandRegistrationEvent.EVENT.register(this::registerCommands);
    }

    private void decorateChat(@Nullable ServerPlayer player, ChatEvent.ChatComponent message) {
        if (player == null) {
            return;
        }

        try {
            ServerStateManager manager = ServerStateManager.get();
            if (manager == null) return;

            Rank highestRank = manager.player(player).getHighestRank();
            Component component = message.get();
            String string = component.getString();
            ChatContext context = new ChatContext()
                    .key("username", player.getName().getString())
                    .key("display-name", player.getDisplayName().getString())
                    .key("rank-name", highestRank.getName())
                    .key("position", () -> "x" + player.getBlockX() + ", y" + player.getBlockY() + ", z" + player.getBlockZ())
                    .key("time", () -> ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_TIME))
                    .key("date", () -> ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE));

            ChatContextEvent.EVENT.invoker().onChatContext(context, player);
            ChatFormatter formatter = new ChatFormatter(string, context);
            message.set(formatter.format().output());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EventResult receiveChat(@Nullable ServerPlayer player, Component message) {
        if (player == null) {
            return null;
        }

        try {
            ServerStateManager manager = ServerStateManager.get();
            if (manager == null) return null;

            String string = message.getString();
            ChatFormatter formatter = new ChatFormatter(string);
            ChatFormatter.Results results = formatter.format();
            if (results.error()) {
                return EventResult.interruptFalse();
            }
            for (ServerPlayer pinged : formatter.format().pinged()) {
                pinged.playNotifySound(SoundEvents.NOTE_BLOCK_BELL, SoundSource.PLAYERS, 2, 2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return EventResult.pass();
    }

    private void onPlayerTick(Player player) {
        if (!hasTicked.contains(player) && player instanceof ServerPlayer p) {
            onJoin(p);
            hasTicked.add(player);
        }
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        TopCommand.register(dispatcher);
        GmCommand.register(dispatcher);
        SudoCommand.register(dispatcher);
        ServerCoreCommand.register(dispatcher);
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
