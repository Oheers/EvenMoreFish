package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.block.implementation.Section;

public class RaritiesFile extends ConfigBase {

    private static RaritiesFile instance = null;

    public RaritiesFile() {
        super("rarities.yml", "rarities.yml", EvenMoreFish.getInstance(), false);
        instance = this;
        updateRequirementFormats();
    }

    private void updateRequirementFormats() {
        Section mainSection = getConfig().getSection("rarities");
        if (mainSection == null) {
            return;
        }
        mainSection.getRoutesAsStrings(false).forEach(rarity -> {
            Section raritySection = getConfig().getSection("rarities." + rarity);
            if (raritySection == null) {
                return;
            }
            Section ingameSection = raritySection.getSection("requirements.ingame-time");
            if (ingameSection != null) {
                int min = ingameSection.getInt("minTime");
                int max = ingameSection.getInt("maxTime");
                ingameSection.remove("minTime");
                ingameSection.remove("maxTime");
                getConfig().set("rarities." + rarity + ".requirements.ingame-time", min + "-" + max);
            }
            Section irlSection = raritySection.getSection("requirements.irl-time");
            if (irlSection != null) {
                String min = irlSection.getString("minTime");
                String max = irlSection.getString("maxTime");
                irlSection.remove("minTime");
                irlSection.remove("maxTime");
                getConfig().set("rarities." + rarity + ".requirements.irl-time", min + "-" + max);
            }
        });
        save();
    }

    public static RaritiesFile getInstance() {
        return instance;
    }
}
