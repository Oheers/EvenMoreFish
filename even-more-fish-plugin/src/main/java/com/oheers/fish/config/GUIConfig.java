package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;

public class GUIConfig extends ConfigBase {

    private static GUIConfig instance = null;

    public GUIConfig() {
        super("guis.yml", "guis.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }
    
    public static GUIConfig getInstance() {
        return instance;
    }

    public String getToggle(boolean toggleState) {
        if (toggleState) return getConfig().getString("enabled-msg", "&a&l✔");
        else return getConfig().getString("disabled-msg", "&c&l✘");
    }

}
