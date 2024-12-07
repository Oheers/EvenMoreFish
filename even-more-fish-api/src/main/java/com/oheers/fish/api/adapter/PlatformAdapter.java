package com.oheers.fish.api.adapter;

import org.jetbrains.annotations.NotNull;

public abstract class PlatformAdapter {

    public PlatformAdapter() {
        logLoadedMessage();
    }

    public abstract void logLoadedMessage();

    public abstract String translateColorCodes(@NotNull String message);

}
