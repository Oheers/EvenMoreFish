package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.Xmas2022Config;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.requirements.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Names {

    public boolean regionCheck;
    // Gets all the fish names.
    Set<String> rarities, fishSet, fishList;
    FileConfiguration fishConfiguration, rarityConfiguration;

    /*
     *  Goes through the fish branch of fish.yml, then for each rarity it realises on its journey,
     *  it goes down that branch looking for fish and their names. It then plops all this stuff into the
     *  main fish map. Badabing badaboom we've now populated our fish map.
     */
    public void loadRarities(FileConfiguration fishConfiguration, FileConfiguration rarityConfiguration) {
        fishList = new HashSet<>();

        // gets all the rarities - just their names, nothing else
        rarities = fishConfiguration.getConfigurationSection("fish").getKeys(false);
        rarities.add("Christmas 2022");

        for (String rarity : rarities) {

            boolean xmasRarity = rarity.equals("Christmas 2022");

            if (xmasRarity) {
                if (Xmas2022Config.getInstance().isAvailable()) {
                    this.rarityConfiguration = Xmas2022Config.getInstance().getConfig();
                    this.fishConfiguration = Xmas2022Config.getInstance().getConfig();
                } else {
                    continue;
                }
            } else {
                this.fishConfiguration = fishConfiguration;
                this.rarityConfiguration = rarityConfiguration;
            }

            // gets all the fish in said rarity, again - just their names
            fishSet = this.fishConfiguration.getConfigurationSection("fish." + rarity).getKeys(false);
            fishList.addAll(fishSet);

            // creates a rarity object and a fish queue
            Rarity r = new Rarity(rarity, rarityColour(rarity), rarityWeight(rarity), rarityAnnounce(rarity), rarityUseConfigCasing(rarity), rarityOverridenLore(rarity));
            if (xmasRarity) {
                EvenMoreFish.getInstance().setXmasRarity(r);
            }
            r.setPermission(rarityPermission(rarity));
            r.setDisplayName(rarityDisplayName(rarity));
            r.setRequirements(getRequirements(null, rarity, RaritiesFile.getInstance().getConfig()));

            List<Fish> fishQueue = new ArrayList<>();

            for (String fish : fishSet) {
                Fish canvas = null;

                // for each fish name, a fish object is made that contains the information gathered from that name
                try {
                    canvas = new Fish(r, fish, xmasRarity);
                } catch (InvalidFishException ignored) {
                    // We're looping through the config, this isn't be an issue.
                }

                assert canvas != null;
                canvas.setRequirements(getRequirements(fish, rarity, FishFile.getInstance().getConfig()));
                weightCheck(canvas, fish, r, rarity);
                fishQueue.add(canvas);

                if (xmasRarity) {
                    canvas.setDay(getDay(fish));
                    EvenMoreFish.getInstance().getXmasFish().put(canvas.getDay(), canvas);
                }

                if (compCheckExempt(fish, rarity)) {
                    r.setHasCompExemptFish(true);
                    canvas.setCompExemptFish(true);
                    EvenMoreFish.getInstance().setRaritiesCompCheckExempt(true);
                }

            }

            // puts the collection of fish and their rarities into the main class
            EvenMoreFish.getInstance().getFishCollection().put(r, fishQueue);

            // memory saving or something
            fishList.clear();
        }
    }

    public void loadBaits(FileConfiguration baitConfiguration) {
        ConfigurationSection section = baitConfiguration.getConfigurationSection("baits.");
        if (section == null) return;

        for (String s : section.getKeys(false)) {
            Bait bait = new Bait(s);

            List<String> rarityList;

            if (!(rarityList = baitConfiguration.getStringList("baits." + s + ".rarities")).isEmpty()) {
                for (String rarityString : rarityList) {
                    boolean foundRarity = false;
                    for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            bait.addRarity(r);
                            foundRarity = true;
                            break;
                        }
                    }
                    if (!foundRarity)
                        EvenMoreFish.getInstance().getLogger().severe(rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                }
            }

            if (baitConfiguration.getConfigurationSection("baits." + s + ".fish") != null) {
                for (String rarityString : baitConfiguration.getConfigurationSection("baits." + s + ".fish").getKeys(false)) {
                    Rarity rarity = null;
                    for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            rarity = r;
                            break;
                        }
                    }

                    if (rarity == null) {
                        EvenMoreFish.getInstance().getLogger().severe(rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                    } else {
                        for (String fishString : baitConfiguration.getStringList("baits." + s + ".fish." + rarityString)) {
                            boolean foundFish = false;
                            for (Fish f : EvenMoreFish.getInstance().getFishCollection().get(rarity)) {
                                if (f.getName().equalsIgnoreCase(fishString)) {
                                    bait.addFish(f);
                                    foundFish = true;
                                    break;
                                }
                            }
                            if (!foundFish)
                                EvenMoreFish.getInstance().getLogger().severe(fishString + " could not be found in the " + rarity.getValue() + " config. It was not added to the " + s + " bait.");
                        }
                    }
                }
            }

            EvenMoreFish.getInstance().getBaits().put(s, bait);
        }
    }

    private String rarityColour(String rarity) {
        String colour = this.rarityConfiguration.getString("rarities." + rarity + ".colour");
        if (colour == null) return "&f";
        return colour;
    }

    private double rarityWeight(String rarity) {
        return this.rarityConfiguration.getDouble("rarities." + rarity + ".weight");
    }

    private boolean rarityAnnounce(String rarity) {
        return this.rarityConfiguration.getBoolean("rarities." + rarity + ".broadcast");
    }

    private String rarityOverridenLore(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".override-lore");
    }

    private boolean rarityUseConfigCasing(String rarity) {
        return this.rarityConfiguration.getBoolean("rarities." + rarity + ".use-this-casing");
    }

    private String rarityDisplayName(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".displayname");
    }

    private String rarityPermission(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".permission");
    }

    private List<Requirement> getRequirements(String name, String rarity, FileConfiguration config) {
        ConfigurationSection requirementSection;
        if (name != null) {
            requirementSection = this.fishConfiguration.getConfigurationSection("fish." + rarity + "." + name + ".requirements");
        } else {
            requirementSection = this.rarityConfiguration.getConfigurationSection("rarities." + rarity + ".requirements");
        }
        List<Requirement> currentRequirements = new ArrayList<>();
        boolean xmas2022 = false;
        if (requirementSection == null) {
            if (rarity.equals("Christmas 2022")) xmas2022 = true;
        } else {
            String configLocator;
            if (name != null) configLocator = "fish." + rarity + "." + name;
            else configLocator = "rarities." + rarity;
            for (String s : requirementSection.getKeys(false)) {
                switch (s.toLowerCase()) {
                    case "biome":
                        currentRequirements.add(new Biome(configLocator + ".requirements.biome", config));
                        break;
                    case "irl-time":
                        currentRequirements.add(new IRLTime(configLocator + ".requirements.irl-time", config));
                        break;
                    case "ingame-time":
                        currentRequirements.add(new InGameTime(configLocator + ".requirements.ingame-time", config));
                        break;
                    case "moon-phase":
                        currentRequirements.add(new MoonPhase(configLocator + ".requirements.moon-phase", config));
                        break;
                    case "permission":
                        currentRequirements.add(new Permission(configLocator + ".requirements.permission", config));
                        break;
                    case "region":
                        currentRequirements.add(new Region(configLocator + ".requirements.region", config));
                        regionCheck = true;
                        break;
                    case "weather":
                        currentRequirements.add(new Weather(configLocator + ".requirements.weather", config));
                        break;
                    case "world":
                        currentRequirements.add(new World(configLocator + ".requirements.world", config));
                        break;
                    case "nearby-players":
                        currentRequirements.add(new NearbyPlayers(configLocator + ".requirements.nearby-players", config));
                        break;
                }
            }
        }

        if (name != null && this.fishConfiguration.getBoolean("fish." + rarity + "." + name + ".disabled", false)) {
            currentRequirements.add(new Disabled("fish." + rarity + "." + name + ".disabled", config));
        } else if (this.rarityConfiguration.getBoolean("rarities." + rarity + ".disabled", false)) {
            currentRequirements.add(new Disabled("rarities." + rarity + ".disabled", config));
        }

        if (xmas2022) currentRequirements.add(new Day("fish." + rarity + "." + name + ".day", Xmas2022Config.getInstance().getConfig()));

        return currentRequirements;
    }

    private void weightCheck(Fish fishObject, String name, Rarity rarityObject, String rarity) {
        if (this.fishConfiguration.getDouble("fish." + rarity + "." + name + ".weight") != 0) {
            rarityObject.setFishWeighted(true);
            fishObject.setWeight(this.fishConfiguration.getDouble("fish." + rarity + "." + name + ".weight"));
        }
    }

    private boolean compCheckExempt(String name, String rarity) {
        return this.fishConfiguration.getBoolean("fish." + rarity + "." + name + ".comp-check-exempt");
    }

    private int getDay(String name) {
        return this.fishConfiguration.getInt("fish.Christmas 2022." + name + ".day");
    }

}
