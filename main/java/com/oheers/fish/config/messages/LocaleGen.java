package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;


public class LocaleGen {

    private static final String[] availableLocales = {
            "messages_de",
            "messages_es",
            "messages_fr",
            "messages_nl",
            "messages_pt-br",
            "messages_ru",
            "messages_vn"
    };

    // creates locale files in a /locales/ folder from a list of available locales
    public void createLocaleFiles(EvenMoreFish plugin) {

        for (String locale : availableLocales) {
            File file = new File(plugin.getDataFolder(), "locales" + File.separator + locale + ".yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    URL url = this.getClass().getClassLoader().getResource("locales/" + locale + ".yml");
                    FileUtils.copyToFile(Objects.requireNonNull(url.openStream()), file);
                } catch (Exception e) {
                    EvenMoreFish.logger.log(Level.SEVERE, "Could not create " + locale + " locale. It will not appear in the locales folder.");
                    e.printStackTrace();
                }
            }
        }
    }
}
