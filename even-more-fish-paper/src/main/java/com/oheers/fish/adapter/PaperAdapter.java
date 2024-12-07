package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;

public class PaperAdapter extends PlatformAdapter {

    public PaperAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getComponentLogger().info("Using improved API provided by Paper.");
    }

}
