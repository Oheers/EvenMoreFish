package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.requirements.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Names {

    // Gets all the fish names.
    Set<String> rarities, fishSet, fishList;

    public boolean regionCheck;

    FileConfiguration fishConfiguration, rarityConfiguration;

    /*
     *  Goes through the fish branch of fish.yml, then for each rarity it realises on its journey,
     *  it goes down that branch looking for fish and their names. It then plops all this stuff into the
     *  main fish map. Badabing badaboom we've now populated our fish map.
     */
    public void loadRarities(FileConfiguration fishConfiguration, FileConfiguration rarityConfiguration) {
        this.fishConfiguration = fishConfiguration;
        this.rarityConfiguration = rarityConfiguration;

        fishList = new HashSet<>();

        // gets all the rarities - just their names, nothing else
        rarities = this.fishConfiguration.getConfigurationSection("fish").getKeys(false);

        for (String rarity : rarities) {

            // gets all the fish in said rarity, again - just their names
            fishSet = this.fishConfiguration.getConfigurationSection("fish." + rarity).getKeys(false);
            fishList.addAll(fishSet);

            // creates a rarity object and a fish queue
            Rarity r = new Rarity(rarity, rarityColour(rarity), rarityWeight(rarity), rarityAnnounce(rarity), rarityOverridenLore(rarity));
            r.setPermission(rarityPermission(rarity));
            r.setDisplayName(rarityDisplayName(rarity));

            List<Fish> fishQueue = new ArrayList<>();

            for (String fish : fishSet) {
                Fish canvas = null;

                // for each fish name, a fish object is made that contains the information gathered from that name
                try {
                    canvas = new Fish(r, fish);
                } catch (InvalidFishException ignored) {
                    // We're looping through the config, this isn't be an issue.
                }

                assert canvas != null;
                canvas.setRequirements(getRequirements(fish, rarity));
                weightCheck(canvas, fish, r, rarity);
                fishQueue.add(canvas);

                if (compCheckExempt(fish, rarity)) {
                    r.setHasCompExemptFish(true);
                    canvas.setCompExemptFish(true);
                    EvenMoreFish.raritiesCompCheckExempt = true;
                }

            }

            // puts the collection of fish and their rarities into the main class
            EvenMoreFish.fishCollection.put(r, fishQueue);

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

            if ((rarityList = baitConfiguration.getStringList("baits." + s + ".rarities")).size() != 0) {
                for (String rarityString : rarityList) {
                    boolean foundRarity = false;
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            bait.addRarity(r);
                            foundRarity = true;
                            break;
                        }
                    }
                    if (!foundRarity) EvenMoreFish.logger.log(Level.SEVERE, rarityString + " is nots a loaded rarity value. It was not added to the " + s + " bait.");
                }
            }

            if (baitConfiguration.getConfigurationSection("baits." + s + ".fish") != null) {
                for (String rarityString : baitConfiguration.getConfigurationSection("baits." + s + ".fish").getKeys(false)) {
                    Rarity rarity = null;
                    for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                        if (r.getValue().equalsIgnoreCase(rarityString)) {
                            rarity = r;
                            break;
                        }
                    }

                    if (rarity == null) {
                        EvenMoreFish.logger.log(Level.SEVERE, rarityString + " is not a loaded rarity value. It was not added to the " + s + " bait.");
                    } else {
                        for (String fishString : baitConfiguration.getStringList("baits." + s + ".fish." + rarityString)) {
                            boolean foundFish = false;
                            for (Fish f : EvenMoreFish.fishCollection.get(rarity)) {
                                if (f.getName().equalsIgnoreCase(fishString)) {
                                    bait.addFish(f);
                                    foundFish = true;
                                    break;
                                }
                            }
                            if (!foundFish) EvenMoreFish.logger.log(Level.SEVERE, fishString + " could not be found in the " + rarity.getValue() + " config. It was not added to the " + s + " bait.");
                        }
                    }
                }
            }

            EvenMoreFish.baits.put(s, bait);
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

    private String rarityDisplayName(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".displayname");
    }

    private String rarityPermission(String rarity) {
        return this.rarityConfiguration.getString("rarities." + rarity + ".permission");
    }

    private List<Requirement> getRequirements(String name, String rarity) {
        ConfigurationSection requirementSection = this.fishConfiguration.getConfigurationSection("fish." + rarity + "." + name + ".requirements");
        if (requirementSection == null) return null;

        List<Requirement> currentRequirements = new ArrayList<>();
        for (String s : requirementSection.getKeys(false)) {
            switch (s.toLowerCase()) {
                case "biome": currentRequirements.add(new com.oheers.fish.requirements.Biome("fish." + rarity + "." + name + ".requirements.biome")); break;
                case "irl-time": currentRequirements.add(new IRLTime("fish." + rarity + "." + name + ".requirements.irl-time")); break;
                case "ingame-time": currentRequirements.add(new InGameTime("fish." + rarity + "." + name + ".requirements.ingame-time")); break;
                case "permission": currentRequirements.add(new Permission("fish." + rarity + "." + name + ".requirements.permission")); break;
                case "region": currentRequirements.add(new Region("fish." + rarity + "." + name + ".requirements.region")); regionCheck = true; break;
                case "weather": currentRequirements.add(new Weather("fish." + rarity + "." + name + ".requirements.weather")); break;
                case "world": currentRequirements.add(new World("fish." + rarity + "." + name + ".requirements.world")); break;
            }
        }

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

}
