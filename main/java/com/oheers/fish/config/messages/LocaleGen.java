package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;


public class LocaleGen {

    private static final String[] availableLocales = {
            "messages_fr",
            "messages_nl"
    };

    // creates locale files in a /locales/ folder from a list of available locales
    public static void createLocaleFiles(EvenMoreFish plugin) {

        for (String locale : availableLocales) {
            File file = new File(plugin.getDataFolder(), "locales" + File.pathSeparator + locale + ".yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    FileUtils.copyToFile(Objects.requireNonNull(plugin.getResource("locales" + File.pathSeparator + locale + ".yml")), file);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Could not create " + locale + " locale. It will not appear in the locales folder.");
                    e.printStackTrace();
                }
            }
        }
    }
}
