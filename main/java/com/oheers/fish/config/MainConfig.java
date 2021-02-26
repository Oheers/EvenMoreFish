package com.oheers.fish.config;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MainConfig {

    public static Plugin plugin = Bukkit.getPluginManager().getPlugin("EvenMoreFish");


    public static boolean enabled = plugin.getConfig().getBoolean("enabled");
    public static boolean lengthAffectsY = plugin.getConfig().getBoolean("weight-pull");

    public static int gravity = plugin.getConfig().getInt("weight-gravity");

    public static boolean database = plugin.getConfig().getBoolean("database");

}
