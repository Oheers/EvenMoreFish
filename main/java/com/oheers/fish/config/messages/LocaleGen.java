package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;

import java.io.File;

public class LocaleGen {

    private static final String[] availableLocales = {
            "messages_de",
            "messages_es",
            "messages_fr",
            "messages_nl",
            "messages_pt-br",
            "messages_ru",
            "messages_tr",
            "messages_vn"
    };

    // creates locale files in a /locales/ folder from a list of available locales
    public static void createLocaleFiles(EvenMoreFish plugin) {
        for (String locale : availableLocales) {
            String path = "locales" + File.separator + locale + ".yml";
            File file = new File(plugin.getDataFolder(), path);
            if (!file.exists()) {
                plugin.saveResource(path, true);
            }
        }
    }
}
