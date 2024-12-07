package com.oheers.fish.api.adapter;

public abstract class PlatformAdapter {

    public PlatformAdapter() {
        logLoadedMessage();
    }

    public abstract void logLoadedMessage();

}
