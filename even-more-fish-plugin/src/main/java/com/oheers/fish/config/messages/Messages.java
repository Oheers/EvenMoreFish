package com.oheers.fish.config.messages;

import com.oheers.fish.config.ConfigBase;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages extends ConfigBase {

    private static Messages instance = null;
    public FileConfiguration config;

    public Messages() {
        super("messages.yml");
        reload();
        instance = this;
    }

    public static Messages getInstance() {
        return instance;
    }

    public int configVersion() {
        return getConfig().getInt("config-version");
    }

    public String getSTDPrefix() {
        return getConfig().getString("prefix-regular") + config.getString("prefix") + "&r";
    }
}
