package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.exceptions.InvalidFishException;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

    // TODO cleanup
    public Rarity getRandomWeightedRarity(Player fisher, double boostRate, Set<Rarity> boostedRarities, Set<Rarity> totalRarities) {
        Map<UUID, Rarity> decidedRarities = EvenMoreFish.getInstance().getDecidedRarities();
        if (fisher != null && decidedRarities.containsKey(fisher.getUniqueId())) {
            Rarity chosenRarity = decidedRarities.get(fisher.getUniqueId());
            decidedRarities.remove(fisher.getUniqueId());
            return chosenRarity;
        }

        List<Rarity> allowedRarities = new ArrayList<>();

        if (fisher != null) {
            String region = FishUtils.getRegionName(fisher.getLocation());
            for (Rarity rarity : FishManager.getInstance().getRarityMap().keySet()) {
                if (boostedRarities != null && boostRate == -1 && !boostedRarities.contains(rarity)) {
                    continue;
                }

                if (!(rarity.getPermission() == null || fisher.hasPermission(rarity.getPermission()))) {
                    continue;
                }

                Requirement requirement = rarity.getRequirement();
                RequirementContext context = new RequirementContext(fisher.getWorld(), fisher.getLocation(), fisher, null, null);
                if (requirement.meetsRequirements(context)) {
                    double regionBoost = MainConfig.getInstance().getRegionBoost(region, rarity.getValue());
                    for (int i = 0; i < regionBoost; i++) {
                        allowedRarities.add(rarity);
                    }
                }
            }
        } else {
            allowedRarities.addAll(totalRarities);
        }

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("There are no rarities for the user " + fisher.getName() + " to fish. They have received no fish.");
            return null;
        }

        double totalWeight = 0;

        for (Rarity r : allowedRarities) {
            if (boostRate != -1.0 && boostedRarities != null && boostedRarities.contains(r)) {
                totalWeight += (r.getWeight() * boostRate);
            } else {
                totalWeight += r.getWeight();
            }
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            if (boostRate != -1.0 && boostedRarities != null && boostedRarities.contains(allowedRarities.get(idx))) {
                r -= allowedRarities.get(idx).getWeight() * boostRate;
            } else {
                r -= allowedRarities.get(idx).getWeight();
            }
            if (r <= 0.0) break;
        }

        if (!Competition.isActive() && EvenMoreFish.getInstance().isRaritiesCompCheckExempt()) {
            if (allowedRarities.get(idx).hasCompExemptFish()) return allowedRarities.get(idx);
        } else if (Competition.isActive() || !MainConfig.getInstance().isCompetitionUnique()) {
            return allowedRarities.get(idx);
        }

        return null;
    }

    // TODO cleanup
    public Fish getRandomWeightedFish(List<Fish> fishList, double boostRate, List<Fish> boostedFish) {
        final double totalWeight = FishUtils.getTotalWeight(fishList, boostRate, boostedFish);

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < fishList.size() - 1; ++idx) {

            if (fishList.get(idx).getWeight() == 0.0d) {
                if (boostRate != -1 && boostedFish != null && boostedFish.contains(fishList.get(idx))) {
                    r -= 1 * boostRate;
                } else {
                    r -= 1;
                }
            } else {
                if (boostRate != -1 && boostedFish != null && boostedFish.contains(fishList.get(idx))) {
                    r -= fishList.get(idx).getWeight() * boostRate;
                } else {
                    r -= fishList.get(idx).getWeight();
                }
            }

            if (r <= 0.0) break;
        }

        return fishList.get(idx);
    }

    // TODO cleanup
    public Fish getFish(Rarity r, Location l, Player p, double boostRate, List<Fish> boostedFish, boolean doRequirementChecks) {
        if (r == null) return null;
        // will store all the fish that match the player's biome or don't discriminate biomes

        List<Fish> available = new ArrayList<>();

        // Protection against /emf admin reload causing the plugin to be unable to get the rarity
        if (FishManager.getInstance().getRarityMap().get(r) == null)
            r = getRandomWeightedRarity(p, 1, null, FishManager.getInstance().getRarityMap().keySet());

        if (doRequirementChecks) {
            RequirementContext context = new RequirementContext(l.getWorld(), l, p, null, null);

            for (Fish f : FishManager.getInstance().getRarityMap().get(r)) {

                if (!(boostRate != -1 || boostedFish == null || boostedFish.contains(f))) {
                    continue;
                }

                Requirement requirement = f.getRequirement();
                if (!requirement.meetsRequirements(context)) {
                    continue;
                }
                available.add(f);
            }
        } else {
            for (Fish f : FishManager.getInstance().getRarityMap().get(r)) {

                if (!(boostRate != -1 || boostedFish == null || boostedFish.contains(f))) {
                    continue;
                }

                available.add(f);
            }
        }

        // if the config doesn't define any fish that can be fished in this biome.
        if (available.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().warning("There are no fish of the rarity " + r.getValue() + " that can be fished at (x=" + l.getX() + ", y=" + l.getY() + ", z=" + l.getZ() + ")");
            return null;
        }

        Fish returningFish;

        // checks whether weight calculations need doing for fish
        returningFish = getRandomWeightedFish(available, boostRate, boostedFish);

        if (Competition.isActive() || !MainConfig.getInstance().isCompetitionUnique() || (EvenMoreFish.getInstance().isRaritiesCompCheckExempt() && returningFish.isCompExemptFish())) {
            return returningFish;
        } else {
            return null;
        }
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

        Section raritiesSection = getRarityConfiguration().getSection("rarities");

        if (raritiesSection == null) {
            return;
        }

        List<Rarity> rarityList = new ArrayList<>();

        // Collect the rarities
        raritiesSection.getRoutesAsStrings(false).forEach(rarityString -> {
            Section raritySection = raritiesSection.getSection(rarityString);
            if (raritySection == null) {
                raritySection = raritiesSection.createSection(rarityString);
            }
            rarityList.add(new Rarity(raritySection));
        });

        // Collect the fish in each rarity
        Section parentFishSection = getFishConfiguration().getSection("fish");
        rarityList.forEach(rarity -> {
            String rarityStr = rarity.getValue();
            Section raritySection = parentFishSection.getSection(rarityStr);
            if (raritySection == null) {
                return;
            }
            List<Fish> rarityFish = new ArrayList<>();
            raritySection.getRoutesAsStrings(false).forEach(fishStr -> {
                Section fishSection = raritySection.getSection(fishStr);
                if (fishSection == null) {
                    fishSection = raritySection.createSection(fishStr);
                }
                try {
                    rarityFish.add(new Fish(rarity, fishSection));
                } catch (InvalidFishException exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.WARNING, exception.getMessage(), exception);
                }
            });
            // Add the rarity and fish list to the map.
            rarityMap.put(rarity, rarityFish);
        });

    }

}