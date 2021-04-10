package com.oheers.fish;

import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.JoinChecker;
import com.oheers.fish.competition.reward.LoadRewards;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.messages.MessageFile;
import com.oheers.fish.database.Database;
import com.oheers.fish.fishing.FishEvent;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Names;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.selling.GUICache;
import com.oheers.fish.selling.InteractHandler;
import com.oheers.fish.selling.SellGUI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class EvenMoreFish extends JavaPlugin {

    public static FishFile fishFile;
    public static RaritiesFile raritiesFile;
    public static MessageFile messageFile;

    public static Permission permission = null;
    public static Economy econ = null;

    public static Map<Integer, Set<String>> fish = new HashMap<>();

    public static Map<Rarity, List<Fish>> fishCollection = new HashMap<>();
    public static Map<Integer, List<Reward>> rewards = new HashMap<>();
    // ^ <Position in competition, list of rewards to be given>

    public static Competition active;

    public static GUICache gui;

    public void onEnable() {

        listeners();
        commands();

        // could not setup permissions.
        if (!setupPermissions()) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "EvenMoreFish couldn't hook into Vault permissions. Disabling to prevent serious problems.");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (!setupEconomy()) {
            Bukkit.getLogger().log(Level.SEVERE, "EvenMoreFish couldn't hook into Vault economy. Disabling to prevent serious problems.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        fishFile = new FishFile(this);
        raritiesFile = new RaritiesFile(this);
        messageFile = new MessageFile(this);

        Names names = new Names();
        names.loadRarities();

        LoadRewards.load();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Help.loadValues();

        AutoRunner.init();

        gui = new GUICache();

        if (MainConfig.database) {

            // Attempts to connect to the database if enabled
            try {
                if (!Database.dbExists()) {
                    Database.createDatabase();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        Bukkit.getServer().getLogger().log(Level.INFO, "EvenMoreFish by Oheers : Enabled");

    }

    public void onDisable() {

        Bukkit.getServer().getLogger().log(Level.INFO, "EvenMoreFish by Oheers : Disabled");

    }

    private void listeners() {

        getServer().getPluginManager().registerEvents(new FishEvent(), this);
        getServer().getPluginManager().registerEvents(new JoinChecker(), this);
        getServer().getPluginManager().registerEvents(new InteractHandler(), this);

    }

    private void commands() {
        getCommand("evenmorefish").setExecutor(new CommandCentre());
        CommandCentre.loadTabCompletes();
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
