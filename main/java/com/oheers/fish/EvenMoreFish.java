package com.oheers.fish;

import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.JoinChecker;
import com.oheers.fish.competition.reward.LoadRewards;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.competition.reward.gui.GUIClick;
import com.oheers.fish.competition.reward.gui.GUIClose;
import com.oheers.fish.competition.reward.gui.RewardGUI;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.messages.LocaleGen;
import com.oheers.fish.config.messages.MessageFile;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.database.Database;
import com.oheers.fish.events.FishEatEvent;
import com.oheers.fish.events.FishInteractEvent;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Names;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.selling.GUICache;
import com.oheers.fish.selling.InteractHandler;
import com.oheers.fish.selling.SellGUI;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class EvenMoreFish extends JavaPlugin {

    public static FishFile fishFile;
    public static RaritiesFile raritiesFile;
    public static MessageFile messageFile;

    public static Messages msgs;
    public static MainConfig mainConfig;

    public static Permission permission = null;
    public static Economy econ = null;

    public static Map<Integer, Set<String>> fish = new HashMap<>();

    public static Map<Rarity, List<Fish>> fishCollection = new HashMap<>();
    public static Map<Integer, List<Reward>> rewards = new HashMap<>();
    // ^ <Position in competition, list of rewards to be given>

    public static boolean checkingEatEvent;
    public static boolean checkingIntEvent;

    public static Competition active;

    public static ArrayList<SellGUI> guis;
    public static ArrayList<RewardGUI> rGuis;

    public static boolean isUpdateAvailable;

    public static WorldGuardPlugin wgPlugin;
    public static String guardPL;
    public static boolean papi;

    public static final int METRIC_ID = 11054;

    public void onEnable() {

        snapshotBreaker();

        fishFile = new FishFile(this);
        raritiesFile = new RaritiesFile(this);
        messageFile = new MessageFile(this);

        msgs = new Messages();
        mainConfig = new MainConfig();
        LocaleGen.createLocaleFiles(this);

        guis = new ArrayList<>();
        rGuis = new ArrayList<>();

        if (mainConfig.isEconomyEnabled()) {
            // could not setup economy.
            if (!setupEconomy()) {
                Bukkit.getLogger().log(Level.WARNING, "EvenMoreFish won't be hooking into economy. If this wasn't by choice in config.yml, please install Economy handling plugins.");
            }
        }

        if (!setupPermissions()) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "EvenMoreFish couldn't hook into Vault permissions. Disabling to prevent serious problems.");
            getServer().getPluginManager().disablePlugin(this);
        }

        // async check for updates on the spigot page
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            checkUpdate();
            checkConfigVers();
        });

        // checks against both support region plugins and sets an active plugin (worldguard is priority)
        if (checkWG()) {
            guardPL = "worldguard";
        } else if (checkRP()) {
            guardPL = "redprotect";
        }

        Names names = new Names();
        names.loadRarities();

        rewards = LoadRewards.load();

        listeners();
        commands();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Help.loadValues();

        AutoRunner.init();

        wgPlugin = getWorldGuard();
        checkPapi();

        Metrics metrics = new Metrics(this, METRIC_ID);

        if (EvenMoreFish.mainConfig.isDatabaseOnline()) {

            // Attempts to connect to the database if enabled
            try {
                if (!Database.dbExists()) {
                    Database.createDatabase();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        getServer().getLogger().log(Level.INFO, "EvenMoreFish by Oheers : Enabled");

    }

    public void onDisable() {

        terminateSellGUIS();

        if (EvenMoreFish.mainConfig.isDatabaseOnline()) {
            try {
                Database.closeConnections();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        getServer().getLogger().log(Level.INFO, "EvenMoreFish by Oheers : Disabled");

    }

    private void listeners() {

        getServer().getPluginManager().registerEvents(new JoinChecker(), this);
        getServer().getPluginManager().registerEvents(new FishingProcessor(), this);
        getServer().getPluginManager().registerEvents(new InteractHandler(this), this);
        getServer().getPluginManager().registerEvents(new UpdateNotify(), this);
        getServer().getPluginManager().registerEvents(new GUIClick(), this);
        getServer().getPluginManager().registerEvents(new GUIClose(), this);

        optionalListeners();
    }

    private void optionalListeners() {
        if (checkingEatEvent) {
            getServer().getPluginManager().registerEvents(new FishEatEvent(this), this);
        }

        if (checkingIntEvent) {
            getServer().getPluginManager().registerEvents(new FishInteractEvent(this), this);
        }

        if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            if (!mainConfig.disableMcMMOTreasure()) {
                getServer().getPluginManager().registerEvents(new McMMOTreasureEvent(), this);
            }
        }
    }

    private void commands() {
        getCommand("evenmorefish").setExecutor(new CommandCentre(this));
        CommandCentre.loadTabCompletes();
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }

    private boolean setupEconomy() {
        if (mainConfig.isEconomyEnabled()) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            econ = rsp.getProvider();
            return econ != null;
        } else return false;

    }

    // gets called on server shutdown to simulate all player's closing their /emf shop GUIs
    private void terminateSellGUIS() {
        for (SellGUI gui : guis) {
            GUICache.attemptPop(gui.getPlayer(), true);
        }
        guis.clear();
    }

    public void reload() {

        terminateSellGUIS();

        setupEconomy();

        fish = new HashMap<>();
        fishCollection = new HashMap<>();
        rewards = new HashMap<>();

        reloadConfig();
        saveDefaultConfig();

        Names names = new Names();
        names.loadRarities();

        HandlerList.unregisterAll(new FishEatEvent(this));
        HandlerList.unregisterAll(new FishInteractEvent(this));
        HandlerList.unregisterAll(new McMMOTreasureEvent());
        optionalListeners();

        msgs = new Messages();
        mainConfig = new MainConfig();

        rewards = LoadRewards.load();

        guis = new ArrayList<>();
    }

    // Checks for updates, surprisingly
    private void checkUpdate() {
        if (!getDescription().getVersion().equals(new UpdateChecker(this, 91310).getVersion())) {
            isUpdateAvailable = true;
        }
    }

    private void checkConfigVers() {
        int MSG_CONFIG_VERSION = 6;
        if (msgs.configVersion() > MSG_CONFIG_VERSION) {
            getLogger().log(Level.WARNING, "Your messages.yml config is not up to date. Certain new configurable features may have been added, and without" +
                    " an updated config, you won't be able to modify them. To update, either delete your messages.yml file and restart the server to create a new" +
                    " fresh one, or go through the recent updates, adding in missing values. https://www.spigotmc.org/resources/evenmorefish.91310/updates/");
        }

        int MAIN_CONFIG_VERSION = 6;
        if (mainConfig.configVersion() > MAIN_CONFIG_VERSION) {
            getLogger().log(Level.WARNING, "Your config.yml config is not up to date. Certain new configurable features may have been added, and without" +
            " an updated config, you won't be able to modify them. To update, either delete your config.yml file and restart the server to create a new" +
                    " fresh one, or go through the recent updates, adding in missing values. https://www.spigotmc.org/resources/evenmorefish.91310/updates/");
        }
    }

    /* Gets the worldguard plugin, returns null and assumes the player has this functionality disabled if it
       can't find the plugin. */
    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    private void checkPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papi = true;
            new PlaceholderReceiver(this).register();
        }

    }

    private boolean checkRP(){
        Plugin pRP = Bukkit.getPluginManager().getPlugin("RedProtect");
        return (pRP != null && mainConfig.regionWhitelist());
    }

    private boolean checkWG(){
        Plugin pWG = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return (pWG != null && mainConfig.regionWhitelist());
    }

    // This causes the plugin to become unloadable when a new update is released, this is only ever included in snapshot versions
    // and official won't use this security measure.
    private void snapshotBreaker() {
        if (!new UpdateChecker(this, 91310).getVersion().equals("1.3.1")) {
            Bukkit.getLogger().log(Level.SEVERE, "This snapshot version has been superseded by a newer version and will no longer function. Please update to the latest version available on the Spigot page.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
