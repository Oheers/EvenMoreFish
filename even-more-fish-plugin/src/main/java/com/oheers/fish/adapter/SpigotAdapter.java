package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class SpigotAdapter extends PlatformAdapter {

    public SpigotAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getLogger().info("Using API provided by Spigot.");
    }
}
