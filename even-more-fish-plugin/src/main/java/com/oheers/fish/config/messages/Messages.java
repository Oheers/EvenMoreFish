package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class Messages extends ConfigBase {

    private static Messages instance = null;

    public Messages() {
        super("messages.yml", "locales/" + "messages_" + MainConfig.getInstance().getLocale() + ".yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static Messages getInstance() {
        return instance;
    }

    public String getSTDPrefix() {
        return getConfig().getString("prefix-regular") + getConfig().getString("prefix") + "&r";
    }
}
