package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.Message;
import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperAdapter extends PlatformAdapter {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = '§';

    public PaperAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getComponentLogger().info("Using improved API provided by Paper.");
    }

    @Override
    public String translateColorCodes(@NotNull String message) {
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

    // TODO not null.
    @Override
    public Message createMessage(@NotNull String message) {
        return null;
    }

    // TODO not null.
    @Override
    public Message createMessage(@NotNull List<String> messageList) {
        return null;
    }

}
