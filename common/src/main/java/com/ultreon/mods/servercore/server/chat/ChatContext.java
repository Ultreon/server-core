package com.ultreon.mods.servercore.server.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Context of chat messages.
 *
 * @since 0.1.0
 */
public class ChatContext {
    final Map<@NotNull String, @NotNull Supplier<@NotNull String>> keyMap = new HashMap<>();
    final Map<@NotNull String, @NotNull Supplier<@NotNull TextColor>> colorMap = new HashMap<>();

    /**
     * Add a key &amp; value.
     *
     * @param key   the key.
     * @param value the value.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext key(@NotNull String key, @NotNull Supplier<@NotNull String> value) {
        keyMap.put(key, value);
        return this;
    }

    /**
     * Add a key &amp; value.
     *
     * @param key   the key.
     * @param value the value.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext key(@NotNull String key, @NotNull String value) {
        keyMap.put(key, () -> value);
        return this;
    }

    /**
     * Add a key &amp; value.
     *
     * @param key   the key.
     * @param value the value.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext key(@NotNull String key, @Nullable Object value) {
        keyMap.put(key, () -> String.valueOf(value));
        return this;
    }

    /**
     * Add a color.
     *
     * @param key   the key.
     * @param value a supplier containing a text color.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext color(@NotNull String key, @NotNull Supplier<@NotNull TextColor> value) {
        colorMap.put(key, value);
        return this;
    }

    /**
     * Add a color.
     *
     * @param key   the key.
     * @param color the color.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext color(@NotNull String key, @NotNull TextColor color) {
        colorMap.put(key, () -> color);
        return this;
    }

    /**
     * Add a color.
     *
     * @param key        the key.
     * @param formatting the color formatting.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext color(@NotNull String key, @NotNull ChatFormatting formatting) {
        colorMap.put(key, () -> Objects.requireNonNull(TextColor.fromLegacyFormat(formatting), "Formatting is not a color"));
        return this;
    }

    /**
     * Add a color.
     *
     * @param key the key.
     * @param rgb the RGB color (use 0x###### where # is a hex digit)
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext color(@NotNull String key, int rgb) {
        colorMap.put(key, () -> TextColor.fromRgb(rgb));
        return this;
    }

    /**
     * Add a color.
     *
     * @param key  the key.
     * @param name a vanilla color getObjName, or hex color.
     * @return {@code this}.
     * @since 0.1.0
     */
    public ChatContext color(@NotNull String key, @NotNull String name) {
        colorMap.put(key, () -> Objects.requireNonNull(TextColor.parseColor(name), "Invalid color getObjName"));
        return this;
    }
}
