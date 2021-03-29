package com.oheers.fish.config;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class MainConfig {

    public static Plugin plugin = Bukkit.getPluginManager().getPlugin("EvenMoreFish");


    public static boolean enabled = plugin.getConfig().getBoolean("enabled");
    public static boolean competitionUnique = plugin.getConfig().getBoolean("fish-only-in-competition");

    public static boolean database = plugin.getConfig().getBoolean("database");

    public static List<String> competitionTimes = plugin.getConfig().getStringList("competitions.times");
    public static int competitionDuration = plugin.getConfig().getInt("competitions.duration");

}
