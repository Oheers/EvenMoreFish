package com.oheers.fish.config;

public class FishFile extends ConfigBase {

    private static FishFile instance = null;

    public FishFile() {
        super("fish.yml");
        instance = this;
    }

    public static FishFile getInstance() {
        return instance;
    }

}
