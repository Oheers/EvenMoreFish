package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MessageFile {

    private final EvenMoreFish plugin;
    private FileConfiguration messageConfig;

    public MessageFile(EvenMoreFish plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {

        File messageFile = getFile();

        if (!messageFile.exists()) {
            messageFile.getParentFile().mkdirs();
            this.plugin.saveResource("messages.yml", false);
        }

        this.messageConfig = new YamlConfiguration();

        try {
            this.messageConfig.load(messageFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }

        EvenMoreFish.messageFile = this;
    }

    public FileConfiguration getConfig() {
        if (this.messageConfig == null) reload();
        return this.messageConfig;
    }

    public File getFile() {
        return new File(this.plugin.getDataFolder(), "messages.yml");
    }

    public void save() {
        this.messageConfig = new YamlConfiguration();
        try {
            this.messageConfig.save(getFile());
        } catch (IOException e) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not save EvenMoreFish/messages.yml");
            e.printStackTrace();
        }
    }

}
