package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class MessageFile {

    private EvenMoreFish plugin;
    private File messageFile;
    private FileConfiguration messageConfig;

    public MessageFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        saveDefault();
    }

    // Makes sure all th
    public void reload() {

        if (this.messageFile == null) this.messageFile = new File(this.plugin.getDataFolder(), "messages.yml");

        this.messageConfig = YamlConfiguration.loadConfiguration(this.messageFile);

        // Getting values from the fish.yml file
        InputStream stream = this.plugin.getResource("messages.yml");
        if (stream != null) {
            // Loads the input stream
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            this.messageConfig.setDefaults(defaultConfig);
        }
    }

    public void saveConfig() {
        if (this.messageConfig == null || this.messageFile == null) return;

        try {
            this.getConfig().save(this.messageFile);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "EvenMoreFish could not save the messages configuration in " + this.messageFile, e);
        }
    }

    public FileConfiguration getConfig() {
        if (this.messageConfig == null) reload();
        return this.messageConfig;
    }

    // Initialises config file
    public void saveDefault() {
        if (this.messageFile == null) this.messageFile = new File(this.plugin.getDataFolder(), "messages.yml");

        if (!this.messageFile.exists()) this.plugin.saveResource("messages.yml", false);
    }

}
