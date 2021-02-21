package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Names {

    // Gets all the fish names.
    Set<String> rarities, fishSet, fishList;

    String error = "0 fish found in fish.yml error, check your spacing.";

    public String get(Rarity r) {
        List<String> fishNames = new ArrayList<>(EvenMoreFish.fish.get(r));
        int ran = (int) (Math.random() * fishNames.size());
        return fishNames.get(ran);
    }

    /*
     *  Goes through the fish branch of fish.yml, then for each rarity it realises on its journey,
     *  it goes down that branch looking for fish and their names. It then plops all this stuff into the
     *  main fish map. Badabing badaboom we've now populated our fish map.
     */
    public void loadRarities() {
        fishList = new HashSet<>();

        rarities = EvenMoreFish.fishFile.getConfig().getConfigurationSection("fish").getKeys(false);

        for (String rarity : rarities) {

            fishSet = EvenMoreFish.fishFile.getConfig().getConfigurationSection("fish." + rarity).getKeys(false);
            fishList.addAll(fishSet);

            Rarity r = new Rarity(rarity, rarityColour(rarity), rarityWeight(rarity));

            EvenMoreFish.fish.put(r, fishSet);

            fishList.clear();
        }
    }

    private String rarityColour(String rarity) {
        String colour = EvenMoreFish.raritiesFile.getConfig().getString("rarities." + rarity + ".colour");
        if (colour == null) return "&f";
        return colour;
    }

    private double rarityWeight(String rarity) {
        double weight = EvenMoreFish.raritiesFile.getConfig().getInt("rarities." + rarity + ".weight");

        return weight;
    }

}
