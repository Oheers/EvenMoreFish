package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class SpigotAdapter extends PlatformAdapter {

    public SpigotAdapter(@NotNull EMFPlugin plugin) {
        super(plugin);
    }

    @Override
    public void logLoadedMessage(@NotNull Logger logger) {
        logger.info("Using API provided by Spigot.");
    }
}
