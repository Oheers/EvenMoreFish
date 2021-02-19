package com.eejayy.fish;

import com.eejayy.fish.config.FishFile;
import com.eejayy.fish.fishing.FishEvent;
import com.eejayy.fish.fishing.items.Fish;
import com.eejayy.fish.fishing.items.Names;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EvenMoreFish extends JavaPlugin {

    public static FishFile fishFile;

    public void onEnable() {


        listeners();

        fishFile = new FishFile(this);
        Names names = new Names();
        names.setNames();

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
