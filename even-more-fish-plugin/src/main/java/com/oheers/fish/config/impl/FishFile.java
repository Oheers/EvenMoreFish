package com.oheers.fish.config.impl;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FishFile extends ConfigFile {

    public FishFile(EvenMoreFish plugin) {
        super(plugin);
    }

    @Override
    public String getFileName() {
        return "fish.yml";
    }

    @Override
    public void reload() {
        super.reload();
        EvenMoreFish.fishFile = this;
    }
}
