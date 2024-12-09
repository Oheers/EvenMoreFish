package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.AbstractMessage;
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

    public PaperAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getComponentLogger().info("Using improved API provided by Paper.");
    }

    // TODO not null.
    @Override
    public AbstractMessage createMessage(@NotNull String message) {
        return null;
    }

    // TODO not null.
    @Override
    public AbstractMessage createMessage(@NotNull List<String> messageList) {
        return null;
    }

}
