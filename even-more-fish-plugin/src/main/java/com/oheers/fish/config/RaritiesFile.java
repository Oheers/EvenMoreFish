package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

public class RaritiesFile extends ConfigBase {

    private static RaritiesFile instance = null;

    public RaritiesFile() {
        super("rarities.yml", "rarities.yml", EvenMoreFish.getInstance(), false);
        instance = this;
    }

    public static RaritiesFile getInstance() {
        return instance;
    }
}
