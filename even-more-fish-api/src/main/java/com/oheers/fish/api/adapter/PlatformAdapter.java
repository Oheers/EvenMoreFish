package com.oheers.fish.api.adapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PlatformAdapter {

    public PlatformAdapter() {
        logLoadedMessage();
    }

    public abstract void logLoadedMessage();

    /**
     * Translates the provided message into a legacy string.
     * @return The provided message as a legacy string.
     */
    public String translateColorCodes(@NotNull String message) {
        return createMessage(message).getLegacyMessage();
    }

    public abstract AbstractMessage createMessage(@NotNull String message);

    public abstract AbstractMessage createMessage(@NotNull List<String> messageList);

}
