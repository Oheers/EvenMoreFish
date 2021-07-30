package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;

public class CompetitionConfig {

    FileConfiguration config = EvenMoreFish.competitionFile.getConfig();

    public int configVersion() {
        return config.getInt("config-version");
    }
}
