package com.oheers.fish;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.addons.DefaultAddons;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.reward.EMFRewardsLoadEvent;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitApplicationListener;
import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionQueue;
import com.oheers.fish.competition.JoinChecker;
import com.oheers.fish.competition.rewardtypes.*;
import com.oheers.fish.config.*;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.database.*;
import com.oheers.fish.events.*;
import com.oheers.fish.exceptions.InvalidTableException;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Names;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.FillerStyle;
import com.oheers.fish.selling.InteractHandler;
import com.oheers.fish.selling.SellGUI;
import com.oheers.fish.utils.AntiCraft;
import com.oheers.fish.utils.HeadDBIntegration;
import com.oheers.fish.utils.ItemFactory;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EvenMoreFish extends JavaPlugin implements EMFPlugin {
    private final Random random = new Random();

    private Permission permission = null;
    private Economy economy;
    private Map<Integer, Set<String>> fish = new HashMap<>();
    private final Map<String, Bait> baits = new HashMap<>();
    private Map<Rarity, List<Fish>> fishCollection = new HashMap<>();
    private Rarity xmasRarity;
    private final Map<Integer, Fish> xmasFish = new HashMap<>();
    private final List<UUID> disabledPlayers = new ArrayList<>();
    private ItemStack customNBTRod;
    private boolean checkingEatEvent;
    private boolean checkingIntEvent;
    // Do some fish in some rarities have the comp-check-exempt: true.
    private boolean raritiesCompCheckExempt = false;
    private Competition active;
    private CompetitionQueue competitionQueue;
    private Logger logger;
    private PluginManager pluginManager;
    private List<SellGUI> guis;
    private int metric_fishCaught = 0;
    private int metric_baitsUsed = 0;
    private int metric_baitsApplied = 0;

    // this is for pre-deciding a rarity and running particles if it will be chosen
    // it's a work-in-progress solution and probably won't stick.
    private Map<UUID, Rarity> decidedRarities;
    private boolean isUpdateAvailable;
    private boolean usingPAPI;
    private boolean usingMcMMO;
    private boolean usingHeadsDB;
    private boolean usingPlayerPoints;
    private boolean usingGriefPrevention;

    private WorldGuardPlugin wgPlugin;
    private String guardPL;
    private DatabaseV3 databaseV3;
    private HeadDatabaseAPI HDBapi;

    private static EvenMoreFish instance;
    private static TaskScheduler scheduler;
    private FillerStyle guiFillerStyle;
    private EMFAPI api;

    private AddonManager addonManager;

    public static EvenMoreFish getInstance() {
        return instance;
    }

    public static TaskScheduler getScheduler() {return scheduler;}

    public AddonManager getAddonManager() {
        return addonManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
        this.api = new EMFAPI();


        guis = new ArrayList<>();
        decidedRarities = new HashMap<>();

        logger = getLogger();
        pluginManager = getServer().getPluginManager();

        usingGriefPrevention = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
        usingPlayerPoints = Bukkit.getPluginManager().isPluginEnabled("PlayerPoints");

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        loadRewardManager();

        RewardManager.getInstance().load();
        getServer().getPluginManager().registerEvents(RewardManager.getInstance(), this);

        new MainConfig();
        new Messages();

        saveAdditionalDefaultAddons();
        this.addonManager = new AddonManager(this);
        this.addonManager.load();

        new FishFile();
        new RaritiesFile();
        new BaitFile();
        new CompetitionConfig();
        new Xmas2022Config();

        if (MainConfig.getInstance().debugSession()) {
            new GUIConfig();
        }

        checkPapi();

        if (MainConfig.getInstance().requireNBTRod()) {
            customNBTRod = createCustomNBTRod();
        }

        if (MainConfig.getInstance().isEconomyEnabled()) {
            // could not set up economy.
            if (!setupEconomy()) {
                EvenMoreFish.getInstance().getLogger().warning("EvenMoreFish won't be hooking into economy. If this wasn't by choice in config.yml, please install Economy handling plugins.");
            }
        }

        if (!setupPermissions()) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "EvenMoreFish couldn't hook into Vault permissions. Disabling to prevent serious problems.");
            getServer().getPluginManager().disablePlugin(this);
        }

        // checks against both support region plugins and sets an active plugin (worldguard is priority)
        if (checkWG()) {
            guardPL = "worldguard";
        } else if (checkRP()) {
            guardPL = "redprotect";
        }

        Names names = new Names();
        names.loadRarities(FishFile.getInstance().getConfig(), RaritiesFile.getInstance().getConfig());
        names.loadBaits(BaitFile.getInstance().getConfig());

        if (!names.regionCheck && MainConfig.getInstance().getAllowedRegions().isEmpty()) {
            guardPL = null;
        }

        competitionQueue = new CompetitionQueue();
        competitionQueue.load();

        // async check for updates on the spigot page
        getScheduler().runTaskAsynchronously(() -> isUpdateAvailable = checkUpdate());

        checkConfigVers();

        listeners();
        loadCommandManager();

        if (!MainConfig.getInstance().debugSession()) {
            metrics();
        }

        AutoRunner.init();

        wgPlugin = getWorldGuard();

        if (MainConfig.getInstance().databaseEnabled()) {

            DataManager.init();

            databaseV3 = new DatabaseV3(this);
            //load user reports into cache
            getScheduler().runTaskAsynchronously(() -> {
                EvenMoreFish.getInstance().getDatabaseV3().createTables(false);
                for (Player player : getServer().getOnlinePlayers()) {
                    UserReport playerReport = databaseV3.readUserReport(player.getUniqueId());
                    if (playerReport == null) {
                        EvenMoreFish.getInstance().getLogger().warning("Could not read report for player (" + player.getUniqueId() + ")");
                        continue;
                    }
                    DataManager.getInstance().putUserReportCache(player.getUniqueId(), playerReport);
                }
            });

        }

        logger.log(Level.INFO, "EvenMoreFish by Oheers : Enabled");
    }

    @Override
    public void onDisable() {
        terminateSellGUIS();
        // Don't use the scheduler here because it will throw errors on disable
        saveUserData(false);

        // Ends the current competition in case the plugin is being disabled when the server will continue running
        if (Competition.isActive()) {
            active.end();
        }

        RewardManager.getInstance().unload();

        if (MainConfig.getInstance().databaseEnabled()) {
            databaseV3.shutdown();
        }
        logger.log(Level.INFO, "EvenMoreFish by Oheers : Disabled");
    }

    private void saveAdditionalDefaultAddons() {
        if (!MainConfig.getInstance().useAdditionalAddons()) {
            return;
        }

        for (final String fileName : Arrays.stream(DefaultAddons.values())
                .map(DefaultAddons::getFullFileName)
                .collect(Collectors.toList())) {
            final File addonFile = new File(getDataFolder(), "addons/" + fileName);
            final File jarFile = new File(getDataFolder(), "addons/" + fileName.replace(".addon", ".jar"));
            if (!jarFile.exists()) {
                try {
                    this.saveResource("addons/" + fileName, true);
                    addonFile.renameTo(jarFile);
                } catch (IllegalArgumentException e) {
                    debug(Level.WARNING, String.format("Default addon %s does not exist.", fileName));
                }
            }
        }
    }

    public static void debug(final String message) {
        debug(Level.INFO, message);
    }

    public static void debug(final Level level, final String message) {
        if (MainConfig.getInstance().debugSession()) {
            getInstance().getLogger().log(level, () -> message);
        }
    }

    private void listeners() {

        getServer().getPluginManager().registerEvents(new JoinChecker(), this);
        getServer().getPluginManager().registerEvents(new FishingProcessor(), this);
        getServer().getPluginManager().registerEvents(new InteractHandler(this), this);
        getServer().getPluginManager().registerEvents(new UpdateNotify(), this);
        getServer().getPluginManager().registerEvents(new SkullSaver(), this);
        getServer().getPluginManager().registerEvents(new BaitApplicationListener(), this);

        optionalListeners();
    }

    private void optionalListeners() {
        if (checkingEatEvent) {
            getServer().getPluginManager().registerEvents(FishEatEvent.getInstance(), this);
        }

        if (checkingIntEvent) {
            getServer().getPluginManager().registerEvents(FishInteractEvent.getInstance(), this);
        }

        if (MainConfig.getInstance().blockCrafting()) {
            getServer().getPluginManager().registerEvents(new AntiCraft(), this);
        }

        if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
            usingMcMMO = true;
            if (MainConfig.getInstance().disableMcMMOTreasure()) {
                getServer().getPluginManager().registerEvents(McMMOTreasureEvent.getInstance(), this);
            }
        }

        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            usingHeadsDB = true;
            getServer().getPluginManager().registerEvents(new HeadDBIntegration(), this);
        }

        if (Bukkit.getPluginManager().getPlugin("AureliumSkills") != null) {
            if (MainConfig.getInstance().disableAureliumSkills()) {
                getServer().getPluginManager().registerEvents(new AureliumSkillsFishingEvent(), this);
            }
        }
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") != null) {
            if (MainConfig.getInstance().disableAureliumSkills()) {
                getServer().getPluginManager().registerEvents(new AuraSkillsFishingEvent(), this);
            }
        }
    }

    private void metrics() {
        Metrics metrics = new Metrics(this, 11054);

        metrics.addCustomChart(new SingleLineChart("fish_caught", () -> {
            int returning = metric_fishCaught;
            metric_fishCaught = 0;
            return returning;
        }));

        metrics.addCustomChart(new SingleLineChart("baits_applied", () -> {
            int returning = metric_baitsApplied;
            metric_baitsApplied = 0;
            return returning;
        }));

        metrics.addCustomChart(new SingleLineChart("baits_used", () -> {
            int returning = metric_baitsUsed;
            metric_baitsUsed = 0;
            return returning;
        }));

        metrics.addCustomChart(new SimplePie("experimental_features", () -> MainConfig.getInstance().doingExperimentalFeatures() ? "true" : "false"));
    }

    private void loadCommandManager() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.enableUnstableAPI("brigadier");
        manager.enableUnstableAPI("help");

        manager.getCommandReplacements().addReplacement("main", "emf|evenmorefish");
        manager.getCommandReplacements().addReplacement("duration", String.valueOf(MainConfig.getInstance().getCompetitionDuration() * 60));
        //desc_admin_<command>_<id>
        manager.getCommandReplacements().addReplacements(
                "desc_admin_bait", new Message(ConfigMessage.HELP_ADMIN_BAIT).getRawMessage(true, true),
                "desc_admin_competition", new Message(ConfigMessage.HELP_ADMIN_COMPETITION).getRawMessage(true, true),
                "desc_admin_clearbaits", new Message(ConfigMessage.HELP_ADMIN_CLEARBAITS).getRawMessage(true, true),
                "desc_admin_fish", new Message(ConfigMessage.HELP_ADMIN_FISH).getRawMessage(true, true),
                "desc_admin_nbtrod", new Message(ConfigMessage.HELP_ADMIN_NBTROD).getRawMessage(true, true),
                "desc_admin_reload", new Message(ConfigMessage.HELP_ADMIN_RELOAD).getRawMessage(true, true),
                "desc_admin_version", new Message(ConfigMessage.HELP_ADMIN_VERSION).getRawMessage(true, true),
                "desc_admin_migrate", new Message(ConfigMessage.HELP_ADMIN_MIGRATE).getRawMessage(true, true),
                "desc_admin_rewardtypes", new Message(ConfigMessage.HELP_ADMIN_REWARDTYPES).getRawMessage(true,true),
                "desc_admin_addons", new Message(ConfigMessage.HELP_ADMIN_ADDONS).getRawMessage(true,true),

                "desc_list_fish", new Message(ConfigMessage.HELP_LIST_FISH).getRawMessage(true, true),
                "desc_list_rarities", new Message(ConfigMessage.HELP_LIST_RARITIES).getRawMessage(true, true),

                "desc_competition_start", new Message(ConfigMessage.HELP_COMPETITION_START).getRawMessage(true, true),
                "desc_competition_end", new Message(ConfigMessage.HELP_COMPETITION_START).getRawMessage(true, true),

                "desc_general_top", new Message(ConfigMessage.HELP_GENERAL_TOP).getRawMessage(true, true),
                "desc_general_help", new Message(ConfigMessage.HELP_GENERAL_HELP).getRawMessage(true, true),
                "desc_general_shop", new Message(ConfigMessage.HELP_GENERAL_SHOP).getRawMessage(true, true),
                "desc_general_toggle", new Message(ConfigMessage.HELP_GENERAL_TOGGLE).getRawMessage(true, true),
                "desc_general_admin", new Message(ConfigMessage.HELP_GENERAL_ADMIN).getRawMessage(true, true),
                "desc_general_next", new Message(ConfigMessage.HELP_GENERAL_NEXT).getRawMessage(true, true),
                "desc_general_sellall", new Message(ConfigMessage.HELP_GENERAL_SELLALL).getRawMessage(true, true)
        );


        manager.getCommandConditions().addCondition(Integer.class, "limits", (c, exec, value) -> {
            if (value == null) {
                return;
            }
            if (c.hasConfig("min") && c.getConfigValue("min", 0) > value) {
                throw new ConditionFailedException("Min value must be " + c.getConfigValue("min", 0));
            }

            if (c.hasConfig("max") && c.getConfigValue("max", 0) < value) {
                throw new ConditionFailedException("Max value must be " + c.getConfigValue("max", 0));
            }
        });
        manager.getCommandContexts().registerContext(Rarity.class, c -> {
            final String rarityId = c.popFirstArg().replace("\"", "");
            Optional<Rarity> potentialRarity = EvenMoreFish.getInstance().getFishCollection().keySet().stream()
                    .filter(rarity -> rarity.getValue().equalsIgnoreCase(rarityId))
                    .findFirst();
            if (!potentialRarity.isPresent()) {
                throw new InvalidCommandArgument("No such rarity: " + rarityId);
            }

            return potentialRarity.get();
        });
        manager.getCommandContexts().registerContext(Fish.class, c -> {
            final Rarity rarity = (Rarity) c.getResolvedArg(Rarity.class);
            final String fishId = c.popFirstArg();
            Optional<Fish> potentialFish = EvenMoreFish.getInstance().getFishCollection().get(rarity).stream()
                    .filter(f -> f.getName().equalsIgnoreCase(fishId.replace("_", " ")) || f.getName().equalsIgnoreCase(fishId))
                    .findFirst();

            if (!potentialFish.isPresent()) {
                throw new InvalidCommandArgument("No such fish: " + fishId);
            }

            return potentialFish.get();
        });
        manager.getCommandCompletions().registerCompletion("baits", c -> EvenMoreFish.getInstance().getBaits().keySet().stream().map(s -> s.replace(" ","_")).collect(Collectors.toList()));
        manager.getCommandCompletions().registerCompletion("rarities", c -> EvenMoreFish.getInstance().getFishCollection().keySet().stream().map(Rarity::getValue).collect(Collectors.toList()));
        manager.getCommandCompletions().registerCompletion("fish", c -> {
            final Rarity rarity = c.getContextValue(Rarity.class);
            return EvenMoreFish.getInstance().getFishCollection().get(rarity).stream().map(f -> f.getName().replace(" ","_")).collect(Collectors.toList());
        });

        manager.registerCommand(new EMFCommand());
        manager.registerCommand(new AdminCommand());
    }


    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }

    private boolean setupEconomy() {
        if (MainConfig.getInstance().isEconomyEnabled()) {
            economy = new Economy(MainConfig.getInstance().economyType());
            return economy.isEnabled();
        } else {
            return false;
        }
    }

    // gets called on server shutdown to simulate all player's closing their /emf shop GUIs
    private void terminateSellGUIS() {
        getServer().getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof SellGUI) {
                player.closeInventory();
            }
        });
    }

    private void saveUserData(boolean scheduler) {
        Runnable save = () -> {
            if (!(MainConfig.getInstance().isDatabaseOnline())) {
                return;
            }

            saveFishReports();
            saveUserReports();

            DataManager.getInstance().uncacheAll();
        };
        if (scheduler) {
            getScheduler().runTask(save);
        } else {
            save.run();
        }
    }

    private void saveFishReports() {
        ConcurrentMap<UUID, List<FishReport>> allReports = DataManager.getInstance().getAllFishReports();
        logger.info("Saving " + allReports.keySet().size() + " fish reports.");
        for (Map.Entry<UUID, List<FishReport>> entry : allReports.entrySet()) {
            databaseV3.writeFishReports(entry.getKey(), entry.getValue());

            try {
                if (!databaseV3.hasUser(entry.getKey(), Table.EMF_USERS)) {
                    databaseV3.createUser(entry.getKey());
                }
            } catch (InvalidTableException exception) {
                logger.log(Level.SEVERE, "Fatal error when storing data for " + entry.getKey() + ", their data in primary storage has been deleted.");
            }
        }
    }

    private void saveUserReports() {
        logger.info("Saving " + DataManager.getInstance().getAllUserReports().size() + " user reports.");
        for (UserReport report : DataManager.getInstance().getAllUserReports()) {
            databaseV3.writeUserReport(report.getUUID(), report);
        }
    }

    public ItemStack createCustomNBTRod() {
        ItemFactory itemFactory = new ItemFactory("nbt-rod-item", false);
        itemFactory.enableDefaultChecks();
        itemFactory.setItemDisplayNameCheck(true);
        itemFactory.setItemLoreCheck(true);
        NBTItem nbtItem = new NBTItem(itemFactory.createItem(null, 0));
        NBTCompound emfCompound = nbtItem.getOrCreateCompound(NbtUtils.Keys.EMF_COMPOUND);
        emfCompound.setBoolean(NbtUtils.Keys.EMF_ROD_NBT, true);
        return nbtItem.getItem();
    }

    public void reload() {

        terminateSellGUIS();

        setupEconomy();

        fish = new HashMap<>();
        fishCollection = new HashMap<>();

        reloadConfig();
        saveDefaultConfig();

        Names names = new Names();
        names.loadRarities(FishFile.getInstance().getConfig(), RaritiesFile.getInstance().getConfig());
        names.loadBaits(BaitFile.getInstance().getConfig());

        HandlerList.unregisterAll(FishEatEvent.getInstance());
        HandlerList.unregisterAll(FishInteractEvent.getInstance());
        HandlerList.unregisterAll(McMMOTreasureEvent.getInstance());
        optionalListeners();

        MainConfig.getInstance().reload();
        Messages.getInstance().reload();
        CompetitionConfig.getInstance().reload();
        Xmas2022Config.getInstance().reload();
        if (MainConfig.getInstance().debugSession()) {
            GUIConfig.getInstance().reload();
        }

        if (MainConfig.getInstance().requireNBTRod()) {
            customNBTRod = createCustomNBTRod();
        }

        competitionQueue.load();
    }

    // Checks for updates, surprisingly
    private boolean checkUpdate() {


        String[] spigotVersion = new UpdateChecker(this, 91310).getVersion().split("\\.");
        String[] serverVersion = getDescription().getVersion().split("\\.");

        for (int i = 0; i < serverVersion.length; i++) {
            if (i < spigotVersion.length) {
                if (Integer.parseInt(spigotVersion[i]) > Integer.parseInt(serverVersion[i])) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private void checkConfigVers() {

        // ConfigBase#updateConfig() will make sure these configs have the newest options
        MainConfig.getInstance().updateConfig();
        Messages.getInstance().updateConfig();

        int COMP_CONFIG_VERSION = 1;
        if (CompetitionConfig.getInstance().configVersion() < COMP_CONFIG_VERSION) {
            getLogger().log(Level.WARNING, "Your competitions.yml config is not up to date. Certain new configurable features may have been added, and without" +
                    " an updated config, you won't be able to modify them. To update, either delete your competitions.yml file and restart the server to create a new" +
                    " fresh one, or go through the recent updates, adding in missing values. https://www.spigotmc.org/resources/evenmorefish.91310/updates/");
            CompetitionConfig.getInstance().reload();
        }

        // Clean up the temp directory
        File tempDir = new File(getDataFolder(), "temp");
        if (tempDir.exists()) {
            tempDir.delete();
        }

    }

    /* Gets the worldguard plugin, returns null and assumes the player has this functionality disabled if it
       can't find the plugin. */
    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    private void checkPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            usingPAPI = true;
            new PlaceholderReceiver(this).register();
        }
    }

    private boolean checkRP() {
        Plugin pRP = Bukkit.getPluginManager().getPlugin("RedProtect");
        return (pRP != null);
    }

    private boolean checkWG() {
        Plugin pWG = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return (pWG != null);
    }

    public Random getRandom() {
        return random;
    }

    public Permission getPermission() {
        return permission;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Map<Integer, Set<String>> getFish() {
        return fish;
    }

    public Map<String, Bait> getBaits() {
        return baits;
    }

    public Map<Rarity, List<Fish>> getFishCollection() {
        return fishCollection;
    }

    public Rarity getXmasRarity() {
        return xmasRarity;
    }

    public void setXmasRarity(Rarity rarity) {
        this.xmasRarity = rarity;
    }

    public Map<Integer, Fish> getXmasFish() {
        return xmasFish;
    }

    public List<UUID> getDisabledPlayers() {
        return disabledPlayers;
    }

    public ItemStack getCustomNBTRod() {
        return customNBTRod;
    }

    public boolean isCheckingEatEvent() {
        return checkingEatEvent;
    }

    public void setCheckingEatEvent(boolean bool) {
        this.checkingEatEvent = bool;
    }

    public boolean isCheckingIntEvent() {
        return checkingIntEvent;
    }

    public void setCheckingIntEvent(boolean bool) {
        this.checkingIntEvent = bool;
    }

    public boolean isRaritiesCompCheckExempt() {
        return raritiesCompCheckExempt;
    }

    public void setRaritiesCompCheckExempt(boolean bool) {
        this.raritiesCompCheckExempt = bool;
    }

    public Competition getActiveCompetition() {
        return active;
    }

    public void setActiveCompetition(Competition competition) {
        this.active = competition;
    }

    public CompetitionQueue getCompetitionQueue() {
        return competitionQueue;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public List<SellGUI> getGuis() {
        return guis;
    }

    public int getMetricFishCaught() {
        return metric_fishCaught;
    }

    public void incrementMetricFishCaught(int value) {
        this.metric_fishCaught = (metric_fishCaught + value);
    }

    public int getMetricBaitsUsed() {
        return metric_baitsUsed;
    }

    public void incrementMetricBaitsUsed(int value) {
        this.metric_baitsUsed = (metric_baitsUsed + value);
    }

    public int getMetricBaitsApplied() {
        return metric_baitsApplied;
    }

    public void incrementMetricBaitsApplied(int value) {
        this.metric_baitsApplied = (metric_baitsApplied + value);
    }

    public Map<UUID, Rarity> getDecidedRarities() {
        return decidedRarities;
    }

    public boolean isUpdateAvailable() {
        return isUpdateAvailable;
    }

    public boolean isUsingPAPI() {
        return usingPAPI;
    }

    public boolean isUsingMcMMO() {
        return usingMcMMO;
    }

    public boolean isUsingHeadsDB() {
        return usingHeadsDB;
    }

    public boolean isUsingPlayerPoints() {
        return usingPlayerPoints;
    }

    public boolean isUsingGriefPrevention() {return usingGriefPrevention;}

    public WorldGuardPlugin getWgPlugin() {
        return wgPlugin;
    }

    public String getGuardPL() {
        return guardPL;
    }

    public DatabaseV3 getDatabaseV3() {
        return databaseV3;
    }

    public HeadDatabaseAPI getHDBapi() {
        return HDBapi;
    }

    public void setHDBapi(HeadDatabaseAPI api) {
        this.HDBapi = api;
    }

    public FillerStyle getGuiFillerStyle() {
        return guiFillerStyle;
    }

    public EMFAPI getApi() {
        return api;
    }


    private void loadRewardManager() {
        // Load RewardManager
        RewardManager.getInstance().load();

        // Load RewardTypes
        new CommandRewardType().register();
        new EffectRewardType().register();
        new HealthRewardType().register();
        new HungerRewardType().register();
        new ItemRewardType().register();
        new MessageRewardType().register();
        new MoneyRewardType().register();
        new PermissionRewardType().register();
        new PlayerPointsRewardType().register();
        new EXPRewardType().register();
    }

    /**
     * Retrieves online players excluding those who are vanished.
     *
     * @return A list of online players excluding those who are vanished.
     */
    public List<Player> getOnlinePlayersExcludingVanish() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (!MainConfig.getInstance().shouldRespectVanish()) {
            return players;
        }

        // Check Essentials
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (plugin instanceof Essentials) {
                Essentials essentials = (Essentials) plugin;
                players = players.stream().filter(player -> !essentials.getUser(player).isVanished()).collect(Collectors.toList());
            }
        }

        // Check CMI
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            players = players.stream().filter(player -> !CMIUser.getUser(player).isVanished()).collect(Collectors.toList());
        }

        return players;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        Bukkit.getPluginManager().callEvent(new EMFRewardsLoadEvent());
    }

}
