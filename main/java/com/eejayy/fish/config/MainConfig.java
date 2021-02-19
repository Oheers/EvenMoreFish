package com.eejayy.fish.config;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MainConfig {

    public static Plugin plugin = Bukkit.getPluginManager().getPlugin("EvenMoreFish");


    public static Boolean enabled = plugin.getConfig().getBoolean("enabled");

}
