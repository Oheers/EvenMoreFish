package com.oheers.fish.config;

public class RaritiesFile extends ConfigBase {

    private static RaritiesFile instance = null;

    public RaritiesFile() {
        super("rarities.yml");
        instance = this;
    }

    public static RaritiesFile getInstance() {
        return instance;
    }
}
