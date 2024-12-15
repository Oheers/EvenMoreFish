package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

public class SpigotAdapter extends PlatformAdapter {

    public SpigotAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        Logger logger = EMFPlugin.getInstance().getLogger();
        logger.info("Using API provided by Spigot.");
        logger.warning("Support for Spigot servers will be removed in the future in favour of Paper.");
        logger.warning("You can download Paper here: https://papermc.io/downloads/paper");
    }

    @Override
    public SpigotMessage createMessage(@NotNull String message) {
        return new SpigotMessage(message, this);
    }

    @Override
    public SpigotMessage createMessage(@NotNull List<String> messageList) {
        return new SpigotMessage(messageList, this);
    }
}
