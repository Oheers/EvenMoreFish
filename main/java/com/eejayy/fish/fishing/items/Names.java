package com.eejayy.fish.fishing.items;

import com.eejayy.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

import static com.eejayy.fish.fishing.items.Rarities.*;

public class Names {

    public static List<String> commons;

    // Gets all the fish names.
    Set<String> section = EvenMoreFish.fishFile.getConfig().getConfigurationSection("fish").getKeys(false);

    public void setNames() {

        // Checks if the section set is empty, the plugin couldn't find any fish, and sends a severe to console
        // if not, it loads them to (currently) the common list
        // @TODO create a hashmap that will be <Rarities, List<String>> to hold all the names of each

        if (section.size() == 0) {
            Bukkit.getLogger().log(Level.SEVERE, "0 fish found in fish.yml error, check your spacing.");
        } else {
            commons = new ArrayList<>();
            commons.addAll(section);
        }
    }

    public String get(Rarities r) {
        int ran = (int) (Math.random() * section.size());
        return commons.get(ran);
    }

}
