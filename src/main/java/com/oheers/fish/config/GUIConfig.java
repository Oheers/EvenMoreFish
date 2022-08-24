package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GUIConfig {

    private final EvenMoreFish plugin;
    private FileConfiguration config;

    public GUIConfig (EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File competitionsFile = new File(this.plugin.getDataFolder(), "guis.yml");

        if (!competitionsFile.exists()) {
            competitionsFile.getParentFile().mkdirs();
            this.plugin.saveResource("guis.yml", false);
        }

        this.config = new YamlConfiguration();

        try {
            this.config.load(competitionsFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getToggle(boolean toggleState) {
        if (toggleState) return this.config.getString("enabled-msg", "&a&l✔");
        else return this.config.getString("disabled-msg", "&c&l✘");
    }
}
