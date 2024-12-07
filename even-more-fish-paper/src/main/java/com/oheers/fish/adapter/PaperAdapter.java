package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class PaperAdapter extends PlatformAdapter {

    public PaperAdapter(@NotNull EMFPlugin plugin) {
        super(plugin);
    }

    @Override
    public void logLoadedMessage(@NotNull Logger logger) {
        logger.info("Using improved API provided by Paper.");
    }

}
