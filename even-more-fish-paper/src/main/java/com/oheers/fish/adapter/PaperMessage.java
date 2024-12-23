package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.adapter.PlatformAdapter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperMessage extends AbstractMessage {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final MiniMessage miniMessage = MiniMessage.builder().strict(true).build();

    protected PaperMessage(@NotNull String message, @NotNull PlatformAdapter platformAdapter) {
        super(message, platformAdapter);
    }

    protected PaperMessage(@NotNull List<String> messageList, @NotNull PlatformAdapter platformAdapter) {
        super(messageList, platformAdapter);
    }

    @Override
    public String formatColours(@NotNull String message) {
        // Replace all Section symbols with Ampersands so
        // MiniMessage doesn't explode.
        message = message.replace(ChatColor.COLOR_CHAR, '&');

        try {
            // Parse MiniMessage
            LegacyComponentSerializer legacyAmpersandSerializer = LegacyComponentSerializer.builder()
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();
            Component component = MiniMessage.builder().strict(true).build().deserialize(message);
            // Get legacy color codes from MiniMessage
            message = legacyAmpersandSerializer.serialize(component);
        } catch (ParsingException exception) {
            // Ignore. If MiniMessage throws an exception, we'll only use legacy colors.
        }

        char COLOR_CHAR = '§';
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    @Override
    public void send(@NotNull CommandSender target) {
        if (getRawMessage().isEmpty() || silentCheck()) {
            return;
        }

        String originalMessage = getRawMessage();

        if (target instanceof Player player) {
            setPlayer(player);
        }

        target.sendMessage(getComponentMessage());

        setMessage(originalMessage);
    }

    @Override
    public void sendActionBar(@NotNull CommandSender target) {
        if (getRawMessage().isEmpty() || silentCheck()) {
            return;
        }

        String originalMessage = getRawMessage();

        if (target instanceof Player player) {
            setPlayer(player);
        }

        target.sendActionBar(getComponentMessage());

        setMessage(originalMessage);
    }

    public Component getComponentMessage() {
        formatVariables();
        formatPlaceholderAPI();

        return legacySerializer.deserialize(getRawMessage());
    }

    @Override
    public String getLegacyMessage() {
        formatVariables();
        formatPlaceholderAPI();
        
        return formatColours(getRawMessage());
    }

    @Override
    public void formatPlaceholderAPI() {
        if (!isPAPIEnabled()) {
            return;
        }
        String message = getRawMessage();
        Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(message);
        while (matcher.find()) {
            // Find matched String
            String matched = matcher.group();
            // Convert to Legacy Component and into a MiniMessage String
            String parsed = formatColours(PlaceholderAPI.setPlaceholders(getRelevantPlayer(), matched));
            // Escape matched String so we don't have issues
            String safeMatched = Matcher.quoteReplacement(matched);
            // Replace all instances of the matched String with the parsed placeholder.
            message = message.replaceAll(safeMatched, parsed);
        }
        setMessage(message);
    }

}
