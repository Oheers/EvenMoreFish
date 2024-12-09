package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotAdapter extends PlatformAdapter {

    public SpigotAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getLogger().info("Using API provided by Spigot.");
    }

    // TODO not null.
    @Override
    public AbstractMessage createMessage(@NotNull String message) {
        return new SpigotMessage(message, this);
    }

    // TODO not null.
    @Override
    public AbstractMessage createMessage(@NotNull List<String> messageList) {
        return new SpigotMessage(messageList, this);
    }
}
