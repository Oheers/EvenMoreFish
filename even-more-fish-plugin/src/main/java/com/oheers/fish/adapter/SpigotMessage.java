package com.oheers.fish.adapter;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.adapter.PlatformAdapter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotMessage extends AbstractMessage {

    private final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");

    protected SpigotMessage(@NotNull String message, @NotNull PlatformAdapter platformAdapter) {
        super(message, platformAdapter);
    }

    protected SpigotMessage(@NotNull List<String> messageList, @NotNull PlatformAdapter platformAdapter) {
        super(messageList, platformAdapter);
    }

    @Override
    public String formatColours(@NotNull String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        String COLOR_CHAR = "§";
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
    public void broadcast() {
        send(Bukkit.getConsoleSender());
        Bukkit.getOnlinePlayers().forEach(this::send);
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

        target.sendMessage(getLegacyMessage());

        setMessage(originalMessage);
    }

    @Override
    public String getLegacyMessage() {
        formatVariables();
        formatPlaceholderAPI();

        return formatColours(getRawMessage());
    }

    @Override
    public void formatPlaceholderAPI() {
        if (isPAPIEnabled()) {
            setMessage(PlaceholderAPI.setPlaceholders(getRelevantPlayer(), getRawMessage()));
        }
    }
}
