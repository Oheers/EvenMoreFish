package com.eejayy.fish;

import com.eejayy.fish.fishing.FishEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EvenMoreFish extends JavaPlugin {

    public void onEnable() {

        listeners();

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
