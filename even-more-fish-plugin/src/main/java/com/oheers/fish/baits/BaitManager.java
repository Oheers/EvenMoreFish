package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.BaitFile;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaitManager {
    
    private static BaitManager instance;

    private final Map<String, Bait> baitMap;
    private boolean loaded;
    
    private BaitManager() {
        baitMap = new HashMap<>();
    }
    
    public static BaitManager getInstance() {
        if (instance == null) {
            instance = new BaitManager();
        }
        return instance;
    }
    
    public void load() {
        if (isLoaded()) {
            return;
        }
        loadBaits();
        logLoadedItems();
        loaded = true;
    }
    
    public void reload() {
        if (!isLoaded()) {
            return;
        }
        baitMap.clear();
        loadBaits();
        logLoadedItems();
    }
    
    public void unload() {
        if (!isLoaded()) {
            return;
        }
        baitMap.clear();
        loaded = false;
    }
    
    public boolean isLoaded() {
        return loaded;
    }

    public Map<String, Bait> getBaitMap() {
        return Map.copyOf(baitMap);
    }

    public @Nullable Bait getBait(@Nullable String baitName) {
        if (baitName == null) {
            return null;
        }
        return baitMap.get(baitName.toUpperCase());
    }

    public @Nullable Bait getBait(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return getBait(BaitNBTManager.getBaitName(itemStack));
    }

    // Getters for config files

    public YamlDocument getBaitConfiguration() {
        return BaitFile.getInstance().getConfig();
    }

    // Loading things

    private void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info("Loaded BaitManager with " + baitMap.size() + " Baits.");
    }

    private void loadBaits() {
        Section section = getBaitConfiguration().getSection("baits");
        if (section == null) {
            return;
        }

        for (String baitName : section.getRoutesAsStrings(false)) {
            Bait bait = new Bait(baitName);

            List<String> rarityList = getBaitConfiguration().getStringList("baits." + baitName + ".rarities");

            if (!rarityList.isEmpty()) {
                for (String rarityName : rarityList) {
                    Rarity rarity = FishManager.getInstance().getRarity(rarityName);
                    if (rarity == null) {
                        EvenMoreFish.getInstance().getLogger().severe(rarityName + " is not a valid rarity. It was not added to the " + baitName + " bait.");
                        continue;
                    }
                    bait.addRarity(rarity);
                }
            }

            Section fishSection = getBaitConfiguration().getSection("baits." + baitName + ".fish");

            if (fishSection != null) {
                for (String rarityString : fishSection.getRoutesAsStrings(false)) {
                    Rarity rarity = FishManager.getInstance().getRarity(rarityString);

                    if (rarity == null) {
                        EvenMoreFish.getInstance().getLogger().severe(rarityString + " is not a loaded rarity value. It was not added to the " + baitName + " bait.");
                    } else {
                        List<String> fishNames = getBaitConfiguration().getStringList("baits." + baitName + ".fish." + rarityString);
                        for (String fishName : fishNames) {
                            Fish fish = FishManager.getInstance().getFish(rarity, fishName);
                            if (fish == null) {
                                EvenMoreFish.getInstance().getLogger().severe(fishName + " could not be found in the " + rarity.getValue() + " config. It was not added to the " + baitName + " bait.");
                                continue;
                            }
                            bait.addFish(fish);
                        }
                    }
                }
            }

            baitMap.put(baitName.toUpperCase(), bait);
        }
    }
    
}
