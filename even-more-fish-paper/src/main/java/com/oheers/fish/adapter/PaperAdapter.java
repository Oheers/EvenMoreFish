package com.oheers.fish.adapter;

import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PaperAdapter extends PlatformAdapter {

    public PaperAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getComponentLogger().info("Using improved API provided by Paper.");
    }

    @Override
    public PaperMessage createMessage(@NotNull String message) {
        return new PaperMessage(message, this);
    }

    @Override
    public PaperMessage createMessage(@NotNull List<String> messageList) {
        return new PaperMessage(messageList, this);
    }

}
