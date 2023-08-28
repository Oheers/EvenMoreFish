package com.oheers.fish.config;


import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class ConfigFile {
    protected final EvenMoreFish plugin;
    protected FileConfiguration config;

    protected ConfigFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public FileConfiguration getConfig() {
        if(this.config == null) {
            reload();
        }
        return this.config;
    }

    public void reload() {
        File file = new File(this.plugin.getDataFolder(), getFileName());

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            this.plugin.saveResource(getFileName(), false);
        }

        this.config = new YamlConfiguration();

        try {
            this.config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe(e::getMessage);
        }
    }

    public abstract String getFileName();
}
