package com.oheers.fish.competition.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.ConfigBase;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Exists solely to switch to 2.0's new competition config files.
 */
public class CompetitionConversions {

    public void performCheck() {
        File competitionFile = new File(EvenMoreFish.getInstance().getDataFolder(), "competitions.yml");
        if (!competitionFile.exists() || !competitionFile.isFile()) {
            return;
        }
        EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of competition configs.");
        File competitionsDir = getCompetitionsDirectory();
        if (!competitionsDir.exists()) {
            competitionsDir.mkdirs();
        }
        ConfigBase config = new ConfigBase(competitionFile, EvenMoreFish.getInstance(), false);
        Section competitionSection = config.getConfig().getSection("competitions");
        if (competitionSection == null) {
            finalizeConversion(config);
            return;
        }
        for (String competitionKey : competitionSection.getRoutesAsStrings(false)) {
            Section section = competitionSection.getSection(competitionKey);
            if (section != null) {
                convertSectionToFile(section);
            }
        }
        finalizeConversion(config);
    }

    private void finalizeConversion(@NotNull ConfigBase competitionsConfig) {
        // Rename the file to competitions.yml.old
        File file = competitionsConfig.getFile();
        file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "competitions.yml.old"));
        file.delete();
    }

    /**
     * @return The 'competitions' directory. This may not exist yet.
     */
    private File getCompetitionsDirectory() {
        return new File(EvenMoreFish.getInstance().getDataFolder(), "competitions");
    }

    private void convertSectionToFile(@NotNull Section section) {
        String id = section.getNameAsString();
        if (id == null) {
            return;
        }
        File file = new File(EvenMoreFish.getInstance().getDataFolder(), "competitions/" + id + ".yml");
        ConfigBase configBase = new ConfigBase(file, EvenMoreFish.getInstance(), false);
        YamlDocument config = configBase.getConfig();
        config.setAll(section.getRouteMappedValues(true));
        config.set("id", id);
        configBase.save();
    }

}
