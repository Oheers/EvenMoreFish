package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.exceptions.InvalidFishException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class FishManager {

    private static FishManager instance;

    private Map<Rarity, List<Fish>> rarityMap;
    private boolean loaded = false;

    private FishManager() {
        rarityMap = new HashMap<>();
    }

    public static FishManager getInstance() {
        if (instance == null) {
            instance = new FishManager();
        }
        return instance;
    }

    public void load() {
        if (isLoaded()) {
            return;
        }
        loadRarities();
        logLoadedItems();
        loaded = true;
    }

    public void reload() {
        if (!isLoaded()) {
            return;
        }
        rarityMap.clear();
        loadRarities();
        logLoadedItems();
    }

    public void unload() {
        if (!isLoaded()) {
            return;
        }
        rarityMap.clear();
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }
    
    // Getters for config files
    
    public YamlDocument getFishConfiguration() {
        return FishFile.getInstance().getConfig();
    }
    
    public YamlDocument getRarityConfiguration() {
        return RaritiesFile.getInstance().getConfig();
    }

    // Getters for Rarities and Fish

    public @Nullable Rarity getRarity(@NotNull String rarityName) {
        for (Rarity rarity : rarityMap.keySet()) {
            if (rarity.getValue().equalsIgnoreCase(rarityName)) {
                return rarity;
            }
        }
        return null;
    }

    public List<Fish> getFishForRarity(@NotNull String rarityName) {
        return getFishForRarity(getRarity(rarityName));
    }

    public List<Fish> getFishForRarity(@Nullable Rarity rarity) {
        if (rarity == null) {
            return List.of();
        }
        return rarityMap.get(rarity);
    }

    public @Nullable Fish getFish(@NotNull String rarityName, @NotNull String fishName) {
        return getFish(getRarity(rarityName), fishName);
    }

    public @Nullable Fish getFish(@Nullable Rarity rarity, @NotNull String fishName) {
        if (rarity == null) {
            return null;
        }
        List<Fish> fishList = getFishForRarity(rarity);
        for (Fish fish : fishList) {
            if (fish.getName().equalsIgnoreCase(fishName)) {
                return fish;
            }
        }
        return null;
    }

    public Map<Rarity, List<Fish>> getRarityMap() {
        return rarityMap;
    }

    // Loading things

    private void logLoadedItems() {
        int allFish = 0;
        for (List<Fish> fishList : rarityMap.values()) {
            allFish += fishList.size();
        }
        EvenMoreFish.getInstance().getLogger().info("Loaded FishManager with " + rarityMap.size() + " Rarities and " + allFish + " Fish.");
    }

    private void loadRarities() {

        // gets all the rarities - just their names, nothing else
        Set<String> rarityNames;
        Section section = getFishConfiguration().getSection("fish");
        if (section == null) {
            rarityNames = new HashSet<>();
        } else {
            rarityNames = section.getRoutesAsStrings(false);
        }

        for (String rarityStr : rarityNames) {

            Set<String> fishNames;

            // gets all the fish in said rarity, again - just their names
            Section raritySection = getFishConfiguration().getSection("fish." + rarityStr);
            if (raritySection == null) {
                fishNames = new HashSet<>();
            } else {
                fishNames = raritySection.getRoutesAsStrings(false);
            }

            // creates a rarity object and a fish queue
            Rarity rarity = new Rarity(rarityStr, getRarityColor(rarityStr), getRarityWeight(rarityStr), getRarityAnnounce(rarityStr), getRarityUseConfigCasing(rarityStr), getRarityLoreOverride(rarityStr));
            rarity.setPermission(getRarityPermission(rarityStr));
            rarity.setDisplayName(getRarityDisplayName(rarityStr));
            rarity.setRequirement(getRequirement(null, rarityStr, RaritiesFile.getInstance().getConfig()));

            List<Fish> fishList = new ArrayList<>();

            for (String fishStr : fishNames) {

                Fish fish;

                // for each fish name, a fish object is made that contains the information gathered from that name
                try {
                    fish = new Fish(rarity, fishStr);
                } catch (InvalidFishException exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.WARNING, exception.getMessage(), exception);
                    continue;
                }

                fish.setRequirement(getRequirement(fishStr, rarityStr, FishFile.getInstance().getConfig()));
                checkFishWeight(fish, rarity);
                fishList.add(fish);

                if (getFishCompCheckExempt(fish, rarity)) {
                    rarity.setHasCompExemptFish(true);
                    fish.setCompExemptFish(true);
                    EvenMoreFish.getInstance().setRaritiesCompCheckExempt(true);
                }

            }

            // puts the collection of fish and their rarities into the main class
            rarityMap.put(rarity, fishList);
        }
    }
    
    // Common getters

    private Requirement getRequirement(String fishName, String rarity, YamlDocument config) {
        Section requirementSection;
        if (fishName != null) {
            requirementSection = getFishConfiguration().getSection("fish." + rarity + "." + fishName + ".requirements");
        } else {
            requirementSection = getRarityConfiguration().getSection("rarities." + rarity + ".requirements");
        }

        Requirement requirement = new Requirement();
        if (requirementSection != null) {
            requirementSection.getRoutesAsStrings(false).forEach(requirementString -> {
                List<String> values = new ArrayList<>();
                String fullPath = requirementSection.getRouteAsString() + "." + requirementString;
                if (config.isList(fullPath)) {
                    values.addAll(config.getStringList(fullPath));
                } else {
                    values.add(config.getString(fullPath));
                }
                requirement.add(requirementString, values);
            });
        }

        return requirement;
    }

    // Rarity getters
    
    private String getRarityColor(@NotNull String rarityName) {
        String color = getRarityConfiguration().getString("rarities." + rarityName + ".colour");
        return color == null ? "&f" : color;
    }

    private double getRarityWeight(@NotNull String rarityName) {
        return getRarityConfiguration().getDouble("rarities." + rarityName + ".weight");
    }

    private boolean getRarityAnnounce(@NotNull String rarityName) {
        return getRarityConfiguration().getBoolean("rarities." + rarityName + ".broadcast");
    }

    private boolean getRarityUseConfigCasing(@NotNull String rarityName) {
        return getRarityConfiguration().getBoolean("rarities." + rarityName + ".use-this-casing");
    }

    private String getRarityLoreOverride(@NotNull String rarityName) {
        return getRarityConfiguration().getString("rarities." + rarityName + ".override-lore");
    }
    
    private String getRarityPermission(@NotNull String rarityName) {
        return getRarityConfiguration().getString("rarities." + rarityName + ".permission");
    }
    
    private String getRarityDisplayName(@NotNull String rarityName) {
        return getRarityConfiguration().getString("rarities." + rarityName + ".displayname");
    }
    
    // Fish getters
    
    private void checkFishWeight(@NotNull Fish fish, @NotNull Rarity rarity) {
        double weight = getFishConfiguration().getDouble("fish." + rarity.getValue() + "." + fish.getName() + ".weight");
        if (weight != 0) {
            rarity.setFishWeighted(true);
            fish.setWeight(weight);
        }
    }

    private boolean getFishCompCheckExempt(@NotNull Fish fish, @NotNull Rarity rarity) {
        return getFishConfiguration().getBoolean("fish." + rarity.getValue() + "." + fish.getName() + ".comp-check-exempt");
    }

}
