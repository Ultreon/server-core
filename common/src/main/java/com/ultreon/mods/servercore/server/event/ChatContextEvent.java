package com.ultreon.mods.servercore.server.event;

import com.ultreon.mods.servercore.server.chat.ChatContext;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Chat context event.
 *
 * @since 0.1.0
 */
public class ChatContextEvent {
    /**
     * The event instance
     *
     * @since 0.1.0
     */
    public static final Event<Handler> EVENT = EventFactory.createLoop();

    /**
     * The handler for the chat context event.
     *
     * @since 0.1.0
     */
    @FunctionalInterface
    @ParametersAreNonnullByDefault
    public interface Handler {
        /**
         * Execute the handler.
         *
         * @param context the context of the event.
         * @param player  the player for context.
         * @since 0.1.0
         */
        void onChatContext(ChatContext context, ServerPlayer player);
    }
}
