package com.oheers.fish.api.adapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PlatformAdapter {

    public PlatformAdapter() {
        logLoadedMessage();
    }

    public abstract void logLoadedMessage();

    public abstract String translateColorCodes(@NotNull String message);

    public abstract AbstractMessage createMessage(@NotNull String message);

    public abstract AbstractMessage createMessage(@NotNull List<String> messageList);

}
