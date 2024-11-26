package com.oheers.fish.api.adapter;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public abstract class PlatformAdapter {

    private final EMFPlugin plugin;

    public PlatformAdapter(@NotNull EMFPlugin plugin) {
        this.plugin = plugin;
        logLoadedMessage(plugin.getLogger());
    }

    public EMFPlugin getPlugin() {
        return plugin;
    }

    public abstract void logLoadedMessage(@NotNull Logger logger);

}
