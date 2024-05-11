package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

public class FishFile extends ConfigBase {

    private static FishFile instance = null;

    public FishFile() {
        super("fish.yml", EvenMoreFish.getInstance());
        instance = this;
    }

    public static FishFile getInstance() {
        return instance;
    }

}
