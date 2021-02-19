package com.eejayy.fish;

import com.eejayy.fish.config.FishFile;
import com.eejayy.fish.config.RaritiesFile;
import com.eejayy.fish.fishing.FishEvent;
import com.eejayy.fish.fishing.items.Names;
import com.eejayy.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class EvenMoreFish extends JavaPlugin {

    public static FishFile fishFile;
    public static RaritiesFile raritiesFile;

    public static Map<Rarity, Set<String>> fish = new HashMap<Rarity, Set<String>>();

    public void onEnable() {


        listeners();

        fishFile = new FishFile(this);
        raritiesFile = new RaritiesFile(this);

        Names names = new Names();
        names.loadRarities();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Bukkit.getServer().getLogger().log(Level.INFO, "EvenMoreFish by Eejayy : Enabled");

    }

    public void onDisable() {

        Bukkit.getServer().getLogger().log(Level.INFO, "EvenMoreFish by Eejayy : Disabled");

    }

    private void listeners() {

        getServer().getPluginManager().registerEvents((Listener) new FishEvent(), this);

    }

    private void commands() {

    }

}
