package com.ultreon.mods.servercore.server.chat;

import com.ultreon.mods.servercore.server.state.ServerStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.ChatFormatting.*;

/**
 * Chat formatter.
 *
 * @since 0.1.0
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public class ChatFormatter {
    private static final MutableComponent ERROR = Component.literal("ERROR").withStyle(style -> style
            .withColor(TextColor.fromRgb(0xeb5234)).withBold(true));
    private final String message;
    private final ChatContext context;
    private final boolean doPing;
    private final boolean onlyFormat;
    private StringBuilder current;
    private MutableComponent output = Component.empty();
    private int pos = 0;
    private char cur;
    private TextColor color = TextColor.fromRgb(0xffffff);
    private boolean bold = false;
    private boolean italic = false;
    private boolean underlined = false;
    private boolean strikethrough = false;
    private boolean obfuscated = false;
    private ClickEvent click = null;
    private HoverEvent hover = null;

    // Color types.
    private final TextColor messageColor = color;
    private final List<ServerPlayer> pinged = new ArrayList<>();
    private boolean error = false;

    /**
     * Create the chat formatter for a message.
     *
     * @param message message to format.
     */
    public ChatFormatter(String message) {
        this(message, new ChatContext());
    }

    /**
     * Create the chat formatter for a message with context.
     *
     * @param message message to format.
     * @param context context to use for keys / custom colors.
     */
    public ChatFormatter(String message, ChatContext context) {
        this(message, context, true);
    }

    /**
     * Create the chat formatter for a message with context.
     *
     * @param message message to format.
     * @param context context to use for keys / custom colors.
     * @param doPing  whether to ping people.
     */
    public ChatFormatter(String message, ChatContext context, boolean doPing) {
        this(message, context, doPing, false);
    }

    /**
     * Create the chat formatter for a message with context.
     *
     * @param message    message to format.
     * @param context    context to use for keys / custom colors.
     * @param doPing     whether to ping people.
     * @param onlyFormat whether to only allow formatting like colors, bold, italic, etc.
     */
    public ChatFormatter(String message, ChatContext context, boolean doPing, boolean onlyFormat) {
        this.message = message;
        this.context = context;
        this.doPing = doPing;
        this.onlyFormat = onlyFormat;

        this.current = new StringBuilder();
    }

    /**
     * Format the message.
     *
     * @return the format results.
     */
    public Results format() {
        while (!isEOF()) {
            this.cur = read();
            switch (this.cur) {
                case '&' -> formatId();
                case '<' -> formatColor();
                case '%' -> {
                    if (!onlyFormat) formatKey();
                    else write(cur);
                }
                case '@' -> {
                    if (doPing && !onlyFormat) formatMention();
                    else write(cur);
                }
                case '{' -> {

                }
                case '*' -> {
                    if (!isEOF()) {
                        if ((cur = read()) == '*') {
                            next();
                            bold = !bold;
                        } else {
                            next();
                            italic = !italic;
                            write(cur);
                        }
                    } else {
                        next();
                        italic = !italic;
                    }
                }
                case '_' -> {
                    if (!isEOF()) {
                        if ((cur = read()) == '_') {
                            next();
                            underlined = !underlined;
                        } else {
                            next();
                            italic = !italic;
                            write(cur);
                        }
                    } else {
                        next();
                        italic = !italic;
                    }
                }
                case '~' -> {
                    if (!isEOF()) {
                        if ((cur = read()) == '~') {
                            next();
                            strikethrough = !strikethrough;
                        } else {
                            write("~" + cur);
                        }
                    } else {
                        write(cur);
                    }
                }
                case '#' -> {
                    if (!isEOF()) {
                        if ((cur = read()) == '#') {
                            next();
                            obfuscated = !obfuscated;
                        } else {
                            write("#" + cur);
                        }
                    } else {
                        write(cur);
                    }
                }
                case '\\' -> write(!isEOF() ? read() : cur);
                default -> write(cur);
            }

            if (error) {
                return new Results(ERROR, pinged, true);
            }
        }

        next();


        if (error) {
            return new Results(ERROR, pinged, true);
        }
        return new Results(output, pinged, false);
    }

    private void formatMention() {
        String name = readUntil(' ');
        ServerStateManager manager = ServerStateManager.get();
        if (manager == null) {
            return;
        }

        MinecraftServer server = manager.server();
        ServerPlayer player = server.getPlayerList().getPlayerByName(name);
        if (player == null || player.isInvisible()) {
            write("@" + name);
            pinged.add(player);
            return;
        }
        MutableComponent component = Component.literal(ChatFormatting.stripFormatting(player.getDisplayName().getString()) + "\n").withStyle(BLUE)
                .append(Component.literal("Rank: " + manager.player(player).getHighestRank().getName()).withStyle(GRAY))
                .append(Component.literal(String.valueOf(player.getUUID())).withStyle(DARK_GRAY));
        MutableComponent mention = Component.literal("@" + ChatFormatting.stripFormatting(player.getDisplayName().getString()))
                .withStyle(style -> style.withColor(0x3495eb).withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component)));
        next(mention);
    }

    private void formatKey() {
        String key = readUntil('%');
        if (cur != '%') {
            write(key);
            return;
        }

        try {
            write(context.keyMap.get(key).get());
        } catch (Exception e) {
            error = true;
        }
    }

    private void formatId() {
        if (isEOF()) {
            write('&');
            return;
        }

        char read = read();
        switch (Character.toLowerCase(read)) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r' -> {
                ChatFormatting byCode = ChatFormatting.getByCode(read);
                next();
                color = TextColor.fromLegacyFormat(Objects.requireNonNull(byCode));
            }
            case '#' -> {
                String s = readOrEnd(6);
                if (s.length() != 6) {
                    write("&#" + s);
                }
                color = TextColor.parseColor("#" + s);
            }
            case '&', '<', '%', '@', '{' -> write(read);
            default -> write("&" + read);
        }
    }

    private void formatColor() {
        String key = readUntil('>');
        if (cur != '>') {
            write("<" + key);
            return;
        }

        next();

        switch (key) {
            case "b", "bold", "fat", "%" -> bold = true;
            case "/b", "/bold", "/fat", "/%" -> bold = false;
            case "i", "italic", "+" -> italic = true;
            case "/i", "/italic", "/+" -> italic = false;
            case "u", "underlined", "underline", "_" -> underlined = true;
            case "/u", "/underlined", "/underline", "/_" -> underlined = false;
            case "s", "strikethrough", "st", "-" -> strikethrough = true;
            case "/s", "/strikethrough", "/st", "/-" -> strikethrough = false;
            case "m", "o", "magic", "obfuscate", "obfuscated", "$" -> obfuscated = true;
            case "/m", "/o", "/magic", "/obfuscate", "/obfuscated", "/$" -> obfuscated = false;
            case "/", "/*", "r", "reset", "clear" -> {
                bold = false;
                italic = false;
                underlined = false;
                strikethrough = false;
                obfuscated = false;
                color = messageColor;
                click = null;
                hover = null;
            }
            case "mc:red" -> color = TextColor.fromLegacyFormat(RED);
            case "mc:yellow" -> color = TextColor.fromLegacyFormat(YELLOW);
            case "mc:lime", "mc:green" -> color = TextColor.fromLegacyFormat(GREEN);
            case "mc:cyan", "mc:aqua" -> color = TextColor.fromLegacyFormat(AQUA);
            case "mc:blue" -> color = TextColor.fromLegacyFormat(BLUE);
            case "mc:magenta", "mc:light-purple" -> color = TextColor.fromLegacyFormat(LIGHT_PURPLE);
            case "mc:dark-red" -> color = TextColor.fromLegacyFormat(DARK_RED);
            case "mc:gold" -> color = TextColor.fromLegacyFormat(GOLD);
            case "mc:dark-green" -> color = TextColor.fromLegacyFormat(DARK_GREEN);
            case "mc:turquoise", "mc:dark-aqua" -> color = TextColor.fromLegacyFormat(DARK_AQUA);
            case "mc:dark-blue" -> color = TextColor.fromLegacyFormat(DARK_BLUE);
            case "mc:purple", "mc:dark-purple" -> color = TextColor.fromLegacyFormat(DARK_PURPLE);
            case "gray-16", "mc:white" -> color = TextColor.fromLegacyFormat(WHITE);
            case "gray-15" -> color = TextColor.fromRgb(0xf0f0f0);
            case "gray-14" -> color = TextColor.fromRgb(0xe0e0e0);
            case "gray-13" -> color = TextColor.fromRgb(0xd0d0d0);
            case "gray-12", "light-gray" -> color = TextColor.fromRgb(0xc0c0c0);
            case "gray-11" -> color = TextColor.fromRgb(0xb0b0b0);
            case "gray-10" -> color = TextColor.fromRgb(0xa0a0a0);
            case "mc:gray", "mc:silver" -> color = TextColor.fromLegacyFormat(GRAY);
            case "gray-9" -> color = TextColor.fromRgb(0x909090);
            case "gray-8" -> color = TextColor.fromRgb(0x808080);
            case "mid-gray", "gray-7" -> color = TextColor.fromRgb(0x707070);
            case "gray-6" -> color = TextColor.fromRgb(0x606060);
            case "mc:dark-gray" -> color = TextColor.fromLegacyFormat(DARK_GRAY);
            case "gray-5" -> color = TextColor.fromRgb(0x505050);
            case "gray-4" -> color = TextColor.fromRgb(0x404040);
            case "darker-gray", "gray-3" -> color = TextColor.fromRgb(0x303030);
            case "gray-2" -> color = TextColor.fromRgb(0x202020);
            case "gray-1" -> color = TextColor.fromRgb(0x101010);
            case "gray-0" -> color = TextColor.fromRgb(0x000000);
            case "alice-blue" -> color = TextColor.fromRgb(0xf0f8ff);
            case "antique-white" -> color = TextColor.fromRgb(0xfaebd7);
            case "aquamarine" -> color = TextColor.fromRgb(0x7fffd4);
            case "azure" -> color = TextColor.fromRgb(0xf0ffff);
            case "beige" -> color = TextColor.fromRgb(0xf5f5dc);
            case "bisque" -> color = TextColor.fromRgb(0xffe4c4);
            case "blanched-almond" -> color = TextColor.fromRgb(0xffebcd);
            case "black" -> color = TextColor.fromLegacyFormat(BLACK);
            case "blue-violet" -> color = TextColor.fromRgb(0x8a2be2);
            case "brown" -> color = TextColor.fromRgb(0xa52a2a);
            case "burly-wood" -> color = TextColor.fromRgb(0xdeb887);
            case "cadet-blue" -> color = TextColor.fromRgb(0x5f9ea0);
            case "chocolate" -> color = TextColor.fromRgb(0xd2691e);
            case "coral" -> color = TextColor.fromRgb(0xff7f50);
            case "cornflower-blue" -> color = TextColor.fromRgb(0x6495ed);
            case "cornsilk" -> color = TextColor.fromRgb(0xfff8dc);
            case "crimson" -> color = TextColor.fromRgb(0xdc143c);
            case "cyan" -> color = TextColor.fromRgb(0x00ffff);
            case "dark-blue" -> color = TextColor.fromRgb(0x00008b);
            case "dark-cyan" -> color = TextColor.fromRgb(0x008b8b);
            case "dark-golden-rod" -> color = TextColor.fromRgb(0xb8860b);
            case "dark-grey" -> color = TextColor.fromRgb(0xa9a9a9);
            case "dark-green" -> color = TextColor.fromRgb(0x006400);
            case "dark-khaki" -> color = TextColor.fromRgb(0xbdb76b);
            case "dark-magenta" -> color = TextColor.fromRgb(0x8b008b);
            case "dark-olive-green" -> color = TextColor.fromRgb(0x556b2f);
            case "dark-orange" -> color = TextColor.fromRgb(0xff8c00);
            case "dark-orchid" -> color = TextColor.fromRgb(0x9932cc);
            case "dark-red" -> color = TextColor.fromRgb(0x8b0000);
            case "dark-salmon" -> color = TextColor.fromRgb(0xe9967a);
            case "dark-sea-green" -> color = TextColor.fromRgb(0x8fbc8f);
            case "dark-slate-blue" -> color = TextColor.fromRgb(0x483d8b);
            case "dark-slate-grey" -> color = TextColor.fromRgb(0x2f4f4f);
            case "dark-turquoise" -> color = TextColor.fromRgb(0x00ced1);
            case "dark-violet" -> color = TextColor.fromRgb(0x9400d3);
            case "dark-yellow" -> color = TextColor.fromRgb(0x808000);
            case "deep-pink" -> color = TextColor.fromRgb(0xff1493);
            case "deep-sky-blue" -> color = TextColor.fromRgb(0x00bfff);
            case "dim-gray" -> color = TextColor.fromRgb(0x696969);
            case "dodger-blue" -> color = TextColor.fromRgb(0x1e90ff);
            case "fire-brick" -> color = TextColor.fromRgb(0xb22222);
            case "floral-white" -> color = TextColor.fromRgb(0xfffaf0);
            case "folly-red" -> color = TextColor.fromRgb(0x00FF4F);
            case "forest-green" -> color = TextColor.fromRgb(0x228b22);
            case "fuchsia" -> color = TextColor.fromRgb(0xff00ff);
            case "gainsboro" -> color = TextColor.fromRgb(0xdcdcdc);
            case "ghost-white" -> color = TextColor.fromRgb(0xf8f8ff);
            case "gold" -> color = TextColor.fromRgb(0xffd700);
            case "golden-rod" -> color = TextColor.fromRgb(0xdaa520);
            case "grey" -> color = TextColor.fromRgb(0x808080);
            case "green" -> color = TextColor.fromRgb(0x008000);
            case "green-yellow" -> color = TextColor.fromRgb(0xadff2f);
            case "honey-dew" -> color = TextColor.fromRgb(0xf0fff0);
            case "hot-pink", "hot-ping" -> color = TextColor.fromRgb(0xff69b4);
            case "indian-red" -> color = TextColor.fromRgb(0xcd5c5c);
            case "indigo" -> color = TextColor.fromRgb(0x4b0082);
            case "ivory" -> color = TextColor.fromRgb(0xfffff0);
            case "khaki" -> color = TextColor.fromRgb(0xf0e68c);
            case "lavender" -> color = TextColor.fromRgb(0xe6e6fa);
            case "lavender-blush" -> color = TextColor.fromRgb(0xfff0f5);
            case "lawn-green" -> color = TextColor.fromRgb(0x7cfc00);
            case "lemon-chiffon" -> color = TextColor.fromRgb(0xfffacd);
            case "light-blue" -> color = TextColor.fromRgb(0xadd8e6);
            case "light-coral" -> color = TextColor.fromRgb(0xf08080);
            case "light-cyan" -> color = TextColor.fromRgb(0xe0ffff);
            case "light-golden-rod-yellow" -> color = TextColor.fromRgb(0xfafad2);
            case "light-grey" -> color = TextColor.fromRgb(0xd3d3d3);
            case "light-green" -> color = TextColor.fromRgb(0x90ee90);
            case "light-pink" -> color = TextColor.fromRgb(0xffb6c1);
            case "light-salmon" -> color = TextColor.fromRgb(0xffa07a);
            case "light-sea-green" -> color = TextColor.fromRgb(0x20b2aa);
            case "light-sky-blue" -> color = TextColor.fromRgb(0x87cefa);
            case "light-slate-grey" -> color = TextColor.fromRgb(0x778899);
            case "light-steel-blue" -> color = TextColor.fromRgb(0xb0c4de);
            case "light-yellow" -> color = TextColor.fromRgb(0xffffe0);
            case "lime" -> color = TextColor.fromRgb(0x00ff00);
            case "lime-green" -> color = TextColor.fromRgb(0x32cd32);
            case "linen" -> color = TextColor.fromRgb(0xfaf0e6);
            case "magenta" -> color = TextColor.fromRgb(0xff00ff);
            case "maroon" -> color = TextColor.fromRgb(0x800000);
            case "medium-aqua-marine" -> color = TextColor.fromRgb(0x66cdaa);
            case "medium-blue" -> color = TextColor.fromRgb(0x0000cd);
            case "medium-orchid" -> color = TextColor.fromRgb(0xba55d3);
            case "medium-purple" -> color = TextColor.fromRgb(0x9370d8);
            case "medium-sea-green" -> color = TextColor.fromRgb(0x3cb371);
            case "medium-slate-blue" -> color = TextColor.fromRgb(0x7b68ee);
            case "medium-spring-green" -> color = TextColor.fromRgb(0x00fa9a);
            case "medium-turquoise" -> color = TextColor.fromRgb(0x48d1cc);
            case "medium-violet-red" -> color = TextColor.fromRgb(0xc71585);
            case "midnight-blue" -> color = TextColor.fromRgb(0x191970);
            case "mint" -> color = TextColor.fromRgb(0x00FF7F);
            case "mint-cream" -> color = TextColor.fromRgb(0xf5fffa);
            case "minty-rose" -> color = TextColor.fromRgb(0xffe4e1);
            case "moccasin" -> color = TextColor.fromRgb(0xffe4b5);
            case "navajo-white" -> color = TextColor.fromRgb(0xffdead);
            case "navy" -> color = TextColor.fromRgb(0x000080);
            case "old-lace" -> color = TextColor.fromRgb(0xfdf5e6);
            case "olive" -> color = TextColor.fromRgb(0x808080);
            case "olive-drab" -> color = TextColor.fromRgb(0x6b8e23);
            case "orange" -> color = TextColor.fromRgb(0xffa500);
            case "orange-red" -> color = TextColor.fromRgb(0xff4500);
            case "orchid" -> color = TextColor.fromRgb(0xda70d6);
            case "pale-golden-rod" -> color = TextColor.fromRgb(0xeee8aa);
            case "pale-gree" -> color = TextColor.fromRgb(0x98bf98);
            case "pale-green" -> color = TextColor.fromRgb(0x98fb98);
            case "pale-turquoise" -> color = TextColor.fromRgb(0xafeeee);
            case "pale-violet-red" -> color = TextColor.fromRgb(0xd87093);
            case "papaya-whip" -> color = TextColor.fromRgb(0xffefd5);
            case "peach-puff" -> color = TextColor.fromRgb(0xffdab9);
            case "peru" -> color = TextColor.fromRgb(0xcd853f);
            case "pink" -> color = TextColor.fromRgb(0xffc0cb);
            case "plum" -> color = TextColor.fromRgb(0xdda0dd);
            case "powder-blue" -> color = TextColor.fromRgb(0xb0e0e6);
            case "red" -> color = TextColor.fromRgb(0xff0000);
            case "rosy-brown" -> color = TextColor.fromRgb(0xbc8f8f);
            case "royal-blue" -> color = TextColor.fromRgb(0x4169e1);
            case "saddle-brown" -> color = TextColor.fromRgb(0x8b4513);
            case "salmon" -> color = TextColor.fromRgb(0xfa8072);
            case "sandy-brown" -> color = TextColor.fromRgb(0xfaa460);
            case "sea-green" -> color = TextColor.fromRgb(0x2e8b57);
            case "sea-shell" -> color = TextColor.fromRgb(0xfff5ee);
            case "slenna" -> color = TextColor.fromRgb(0xa0522d);
            case "silver" -> color = TextColor.fromRgb(0xc0c0c0);
            case "sky-blue" -> color = TextColor.fromRgb(0x87ceeb);
            case "slate-blue" -> color = TextColor.fromRgb(0x6a5acd);
            case "slate-grey" -> color = TextColor.fromRgb(0x708090);
            case "snow" -> color = TextColor.fromRgb(0xfffafa);
            case "spring-green" -> color = TextColor.fromRgb(0x00ff7f);
            case "sleet-blue" -> color = TextColor.fromRgb(0x4682b4);
            case "tan" -> color = TextColor.fromRgb(0xd2b48c);
            case "teal" -> color = TextColor.fromRgb(0x008080);
            case "thistle" -> color = TextColor.fromRgb(0xd8bfd8);
            case "tomato" -> color = TextColor.fromRgb(0xff6347);
            case "turquoise" -> color = TextColor.fromRgb(0x40e0d0);
            case "ultreon" -> color = TextColor.fromRgb(0xff7f00);
            case "violet" -> color = TextColor.fromRgb(0xee82ee);
            case "wheat" -> color = TextColor.fromRgb(0xf5deb3);
            case "white" -> color = TextColor.fromRgb(0xffffff);
            case "white-smoke" -> color = TextColor.fromRgb(0xf5f5f5);
            case "yellow" -> color = TextColor.fromRgb(0xffff00);
            case "yellow-green" -> color = TextColor.fromRgb(0x9acd32);
            case "yellow-gold" -> color = TextColor.fromRgb(0xffd500);
            case "code:method" -> color = TextColor.fromRgb(0x61AFEF);
            case "code:string-escape" -> color = TextColor.fromRgb(0x2BBAC5);
            case "code:string" -> color = TextColor.fromRgb(0x89CA78);
            case "code:class" -> color = TextColor.fromRgb(0xE5C07B);
            case "code:number" -> color = TextColor.fromRgb(0xD19A66);
            case "code:enum-value" -> color = TextColor.fromRgb(0xEF596F);
            case "code:keyword" -> color = TextColor.fromRgb(0xD55FDE);
            default -> {
                TextColor parsed = TextColor.parseColor(key);
                if (parsed != null) color = parsed;
                else error = true;
            }
        }
    }

    private void next() {
        String s = this.current.toString();
        MutableComponent component = Component.literal(s);
        component.withStyle(style -> style.withColor(color).withBold(bold).withItalic(italic).withUnderlined(underlined)
                .withStrikethrough(strikethrough).withObfuscated(obfuscated).withClickEvent(click).withHoverEvent(hover));
        this.output = this.output.append(component);
        this.current = new StringBuilder();
    }

    private void next(MutableComponent component) {
        next();
        this.output = this.output.append(component);
    }

    private char read() {
        char c = this.message.charAt(this.pos);
        this.pos++;
        return c;
    }

    @Nullable
    private String read(int amount) {
        if (this.pos + amount > message.length()) {
            return null;
        }
        String s = this.message.substring(this.pos, this.pos + amount);
        this.pos += amount;
        return s;
    }

    @NotNull
    private String readOrEnd(int amount) {
        if (this.pos + amount > message.length()) {
            return readToEnd();
        }
        return Objects.requireNonNull(read(amount));
    }

    private String readToEnd() {
        String s = this.message.substring(this.pos);
        this.pos += message.length();
        return s;
    }

    @SuppressWarnings("SameParameterValue")
    private String readUntil(char c) {
        StringWriter writer = new StringWriter();
        while (!isEOF() && (cur = read()) != c) {
            writer.append(cur);
        }
        return writer.toString();
    }

    private void write(Object o) {
        current.append(o);
    }

    private void write(String o) {
        current.append(o);
    }

    private void write(char o) {
        current.append(o);
    }

    private boolean isEOF() {
        return this.pos >= this.message.length();
    }

    /**
     * Get the source message.
     *
     * @return the message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * The formatter results.
     *
     * @param output the output chat component/
     * @param pinged the pinged players.
     * @param error  whether an error occurred.
     */
    public record Results(MutableComponent output, List<ServerPlayer> pinged, boolean error) {
        /**
         * Get the string version of the {@link #output() output}.
         *
         * @return the string version of the output.
         */
        public String string() {
            return output.getString();
        }
    }
}
