package com.oheers.fish;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.oheers.fish.adapter.PaperAdapter;
import com.oheers.fish.adapter.SpigotAdapter;
import com.oheers.fish.addons.AddonManager;
import com.oheers.fish.addons.DefaultAddons;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.requirement.RequirementManager;
import com.oheers.fish.api.reward.RewardManager;
import com.oheers.fish.baits.BaitListener;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.EMFCommand;
import com.oheers.fish.competition.AutoRunner;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionQueue;
import com.oheers.fish.competition.JoinChecker;
import com.oheers.fish.competition.rewardtypes.*;
import com.oheers.fish.competition.rewardtypes.external.*;
import com.oheers.fish.config.BaitFile;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.DatabaseV3;
import com.oheers.fish.database.model.FishReport;
import com.oheers.fish.database.model.UserReport;
import com.oheers.fish.economy.GriefPreventionEconomyType;
import com.oheers.fish.economy.PlayerPointsEconomyType;
import com.oheers.fish.economy.VaultEconomyType;
import com.oheers.fish.events.*;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.requirements.*;
import com.oheers.fish.utils.AntiCraft;
import com.oheers.fish.utils.HeadDBIntegration;
import com.oheers.fish.utils.ItemFactory;
import com.oheers.fish.utils.nbt.NbtKeys;
import de.themoep.inventorygui.InventoryGui;
import de.tr7zw.changeme.nbtapi.NBT;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.milkbowl.vault.permission.Permission;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.firedev.vanishchecker.VanishChecker;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EvenMoreFish extends EMFPlugin {
    private final Random random = new Random();

    private Permission permission = null;
    private ItemStack customNBTRod;
    private boolean checkingEatEvent;
    private boolean checkingIntEvent;
    // Do some fish in some rarities have the comp-check-exempt: true.
    private boolean raritiesCompCheckExempt = false;
    private CompetitionQueue competitionQueue;
    private Logger logger;
    private PluginManager pluginManager;
    private int metric_fishCaught = 0;
    private int metric_baitsUsed = 0;
    private int metric_baitsApplied = 0;

    // this is for pre-deciding a rarity and running particles if it will be chosen
    // it's a work-in-progress solution and probably won't stick.
    private Map<UUID, Rarity> decidedRarities;
    private boolean isUpdateAvailable;
    private boolean usingVault;
    private boolean usingPAPI;
    private boolean usingMcMMO;
    private boolean usingHeadsDB;
    private boolean usingPlayerPoints;
    private boolean usingGriefPrevention;

    private DatabaseV3 databaseV3;
    private HeadDatabaseAPI HDBapi;

    private static EvenMoreFish instance;
    private static TaskScheduler scheduler;
    private static PlatformAdapter platformAdapter;
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

        if (!NBT.preloadApi()) {
            throw new RuntimeException("NBT-API wasn't initialized properly, disabling the plugin");
        }

        // This should only ever be done once.
        EMFPlugin.setInstance(this);
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
        platformAdapter = loadAdapter();

        this.api = new EMFAPI();

        decidedRarities = new HashMap<>();

        logger = getLogger();
        pluginManager = getServer().getPluginManager();

        usingVault = Bukkit.getPluginManager().isPluginEnabled("Vault");
        usingGriefPrevention = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");
        usingPlayerPoints = Bukkit.getPluginManager().isPluginEnabled("PlayerPoints");

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        new MainConfig();
        new Messages();

        saveAdditionalDefaultAddons();
        this.addonManager = new AddonManager(this);
        this.addonManager.load();

        new BaitFile();

        new GUIConfig();
        new GUIFillerConfig();

        checkPapi();

        if (MainConfig.getInstance().requireNBTRod()) {
            customNBTRod = createCustomNBTRod();
        }

        loadEconomy();

        // could not set up economy.
        if (!Economy.getInstance().isEnabled()) {
            EvenMoreFish.getInstance().getLogger().warning("EvenMoreFish won't be hooking into economy. If this wasn't by choice in config.yml, please install Economy handling plugins.");
        }

        setupPermissions();

        loadRequirementManager();

        FishManager.getInstance().load();
        BaitManager.getInstance().load();

        // Do this before anything competition related.
        loadRewardManager();

        competitionQueue = new CompetitionQueue();
        competitionQueue.load();

        // async check for updates on the spigot page
        getScheduler().runTaskAsynchronously(() -> isUpdateAvailable = checkUpdate());

        listeners();
        loadCommandManager();

        if (!MainConfig.getInstance().debugSession()) {
            metrics();
        }

        AutoRunner.init();

        if (MainConfig.getInstance().databaseEnabled()) {

            DataManager.init();

            databaseV3 = new DatabaseV3(this);
            //load user reports into cache
            getScheduler().runTaskAsynchronously(() -> {
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

        // Prevent issues when NBT preload fails.
        if (instance == null) {
            return;
        }

        terminateGUIS();
        // Don't use the scheduler here because it will throw errors on disable
        saveUserData(false);

        // Ends the current competition in case the plugin is being disabled when the server will continue running
        Competition active = Competition.getCurrentlyActive();
        if (active != null) {
            active.end(false);
        }

        RewardManager.getInstance().unload();

        if (MainConfig.getInstance().databaseEnabled()) {
            databaseV3.shutdown();
        }

        FishManager.getInstance().unload();
        BaitManager.getInstance().unload();

        logger.log(Level.INFO, "EvenMoreFish by Oheers : Disabled");
    }

    private void saveAdditionalDefaultAddons() {
        if (!MainConfig.getInstance().useAdditionalAddons()) {
            return;
        }

        for (final String fileName : Arrays.stream(DefaultAddons.values())
                .map(DefaultAddons::getFullFileName)
                .toList()) {
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
        getServer().getPluginManager().registerEvents(new UpdateNotify(), this);
        getServer().getPluginManager().registerEvents(new SkullSaver(), this);
        getServer().getPluginManager().registerEvents(new BaitListener(), this);

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

        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            usingMcMMO = true;
            if (MainConfig.getInstance().disableMcMMOTreasure()) {
                getServer().getPluginManager().registerEvents(McMMOTreasureEvent.getInstance(), this);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            usingHeadsDB = true;
            getServer().getPluginManager().registerEvents(new HeadDBIntegration(), this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AureliumSkills")) {
            if (MainConfig.getInstance().disableAureliumSkills()) {
                getServer().getPluginManager().registerEvents(new AureliumSkillsFishingEvent(), this);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("AuraSkills")) {
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

        metrics.addCustomChart(new SimplePie("database", () -> MainConfig.getInstance().databaseEnabled() ? "true" : "false"));

        metrics.addCustomChart(new SimplePie("paper-adapter", () -> (platformAdapter instanceof PaperAdapter) ? "true" : "false"));
    }

    private void loadCommandManager() {
        PaperCommandManager manager = new PaperCommandManager(this);

        // Brigadier should stay disabled until ACF updates their implementation.
        //manager.enableUnstableAPI("brigadier");
        manager.enableUnstableAPI("help");

        StringBuilder main = new StringBuilder(MainConfig.getInstance().getMainCommandName());
        List<String> aliases = MainConfig.getInstance().getMainCommandAliases();
        if (!aliases.isEmpty()) {
            aliases.forEach(alias -> main.append("|").append(alias));
        }
        manager.getCommandReplacements().addReplacement("main", main.toString());
        manager.getCommandReplacements().addReplacement("duration", String.valueOf(MainConfig.getInstance().getCompetitionDuration() * 60));
        //desc_admin_<command>_<id>
        manager.getCommandReplacements().addReplacements(
                "desc_admin_bait", ConfigMessage.HELP_ADMIN_BAIT.getMessage().getLegacyMessage(),
                "desc_admin_competition", ConfigMessage.HELP_ADMIN_COMPETITION.getMessage().getLegacyMessage(),
                "desc_admin_clearbaits", ConfigMessage.HELP_ADMIN_CLEARBAITS.getMessage().getLegacyMessage(),
                "desc_admin_fish", ConfigMessage.HELP_ADMIN_FISH.getMessage().getLegacyMessage(),
                "desc_admin_nbtrod", ConfigMessage.HELP_ADMIN_NBTROD.getMessage().getLegacyMessage(),
                "desc_admin_reload", ConfigMessage.HELP_ADMIN_RELOAD.getMessage().getLegacyMessage(),
                "desc_admin_version", ConfigMessage.HELP_ADMIN_VERSION.getMessage().getLegacyMessage(),
                "desc_admin_migrate", ConfigMessage.HELP_ADMIN_MIGRATE.getMessage().getLegacyMessage(),
                "desc_admin_rewardtypes", ConfigMessage.HELP_ADMIN_REWARDTYPES.getMessage().getLegacyMessage(),
                "desc_admin_addons", ConfigMessage.HELP_ADMIN_ADDONS.getMessage().getLegacyMessage(),

                "desc_list_fish", ConfigMessage.HELP_LIST_FISH.getMessage().getLegacyMessage(),
                "desc_list_rarities", ConfigMessage.HELP_LIST_RARITIES.getMessage().getLegacyMessage(),

                "desc_competition_start", ConfigMessage.HELP_COMPETITION_START.getMessage().getLegacyMessage(),
                "desc_competition_end", ConfigMessage.HELP_COMPETITION_END.getMessage().getLegacyMessage(),

                "desc_general_top", ConfigMessage.HELP_GENERAL_TOP.getMessage().getLegacyMessage(),
                "desc_general_help", ConfigMessage.HELP_GENERAL_HELP.getMessage().getLegacyMessage(),
                "desc_general_shop", ConfigMessage.HELP_GENERAL_SHOP.getMessage().getLegacyMessage(),
                "desc_general_toggle", ConfigMessage.HELP_GENERAL_TOGGLE.getMessage().getLegacyMessage(),
                "desc_general_gui", ConfigMessage.HELP_GENERAL_GUI.getMessage().getLegacyMessage(),
                "desc_general_admin", ConfigMessage.HELP_GENERAL_ADMIN.getMessage().getLegacyMessage(),
                "desc_general_next", ConfigMessage.HELP_GENERAL_NEXT.getMessage().getLegacyMessage(),
                "desc_general_sellall", ConfigMessage.HELP_GENERAL_SELLALL.getMessage().getLegacyMessage(),
                "desc_general_applybaits", ConfigMessage.HELP_GENERAL_APPLYBAITS.getMessage().getLegacyMessage()
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
            Rarity rarity = FishManager.getInstance().getRarity(rarityId);
            if (rarity == null) {
                throw new InvalidCommandArgument("No such rarity: " + rarityId);
            }
            return rarity;
        });
        manager.getCommandContexts().registerContext(Fish.class, c -> {
            final Rarity rarity = (Rarity) c.getResolvedArg(Rarity.class);
            final String fishId = c.popFirstArg();
            Fish fish = rarity.getFish(fishId);
            if (fish == null) {
                fish = rarity.getFish(fishId.replace("_", " "));
            }
            if (fish == null) {
                throw new InvalidCommandArgument("No such fish: " + fishId);
            }
            return fish;
        });
        manager.getCommandCompletions().registerCompletion("baits", c -> BaitManager.getInstance().getBaitMap().keySet().stream().map(s -> s.replace(" ", "_")).toList());
        manager.getCommandCompletions().registerCompletion("rarities", c -> FishManager.getInstance().getRarityMap().values().stream().map(Rarity::getId).toList());
        manager.getCommandCompletions().registerCompletion("fish", c -> {
            final Rarity rarity = c.getContextValue(Rarity.class);
            return rarity.getFishList().stream().map(f -> f.getName().replace(" ", "_")).toList();
        });
        manager.getCommandCompletions().registerCompletion("competitionId", c -> getCompetitionQueue().getFileMap().keySet());

        manager.registerCommand(new EMFCommand());
        manager.registerCommand(new AdminCommand());
    }


    private boolean setupPermissions() {
        if (!usingVault) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp == null ? null : rsp.getProvider();
        return permission != null;
    }

    // gets called on server shutdown to simulate all players closing their GUIs
    private void terminateGUIS() {
        getServer().getOnlinePlayers().forEach(player -> {
            InventoryGui inventoryGui = InventoryGui.getOpen(player);
            if (inventoryGui != null) {
                inventoryGui.close();
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
        logger.info("Saving " + allReports.size() + " fish reports.");
        for (Map.Entry<UUID, List<FishReport>> entry : allReports.entrySet()) {
            databaseV3.writeFishReports(entry.getKey(), entry.getValue());


            if (!databaseV3.hasUser(entry.getKey())) {
                databaseV3.createUser(entry.getKey());
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
        ItemFactory itemFactory = new ItemFactory("nbt-rod-item", MainConfig.getInstance().getConfig());
        itemFactory.enableDefaultChecks();
        itemFactory.setItemDisplayNameCheck(true);
        itemFactory.setItemLoreCheck(true);

        ItemStack customRod = itemFactory.createItem(null, 0);
        NBT.modify(customRod,nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setBoolean(NbtKeys.EMF_ROD_NBT, true);
        });
        return customRod;
    }

    public void reload(@Nullable CommandSender sender) {

        terminateGUIS();

        reloadConfig();
        saveDefaultConfig();

        MainConfig.getInstance().reload();
        Messages.getInstance().reload();
        GUIConfig.getInstance().reload();
        GUIFillerConfig.getInstance().reload();
        BaitFile.getInstance().reload();

        FishManager.getInstance().reload();
        BaitManager.getInstance().reload();

        HandlerList.unregisterAll(FishEatEvent.getInstance());
        HandlerList.unregisterAll(FishInteractEvent.getInstance());
        HandlerList.unregisterAll(McMMOTreasureEvent.getInstance());
        optionalListeners();

        if (MainConfig.getInstance().requireNBTRod()) {
            customNBTRod = createCustomNBTRod();
        }

        competitionQueue.load();

        if (sender != null) {
            ConfigMessage.RELOAD_SUCCESS.getMessage().send(sender);
        }

    }

    // Checks for updates, surprisingly
    private boolean checkUpdate() {
        ComparableVersion spigotVersion = new ComparableVersion(new UpdateChecker(this, 91310).getVersion());
        ComparableVersion serverVersion = new ComparableVersion(getDescription().getVersion());
        return spigotVersion.compareTo(serverVersion) > 0;
    }

    private void checkPapi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            usingPAPI = true;
            new PlaceholderReceiver(this).register();
        }
    }

    public Random getRandom() {
        return random;
    }

    public Permission getPermission() {
        return permission;
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

    public CompetitionQueue getCompetitionQueue() {
        return competitionQueue;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
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

    public boolean isUsingVault() {return usingVault;}

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

    public DatabaseV3 getDatabaseV3() {
        return databaseV3;
    }

    public HeadDatabaseAPI getHDBapi() {
        return HDBapi;
    }

    public void setHDBapi(HeadDatabaseAPI api) {
        this.HDBapi = api;
    }

    public EMFAPI getApi() {
        return api;
    }

    private void loadEconomy() {
        PluginManager pm = Bukkit.getPluginManager();

        if (pm.isPluginEnabled("Vault")) {
            new VaultEconomyType().register();
        }
        if (pm.isPluginEnabled("PlayerPoints")) {
            new PlayerPointsEconomyType().register();
        }
        if (pm.isPluginEnabled("GriefPrevention")) {
            new GriefPreventionEconomyType().register();
        }
    }

    private void loadRewardManager() {
        // Load RewardManager
        RewardManager.getInstance().load();
        getServer().getPluginManager().registerEvents(RewardManager.getInstance(), this);

        // Load RewardTypes
        new CommandRewardType().register();
        new EffectRewardType().register();
        new HealthRewardType().register();
        new HungerRewardType().register();
        new ItemRewardType().register();
        new MessageRewardType().register();
        new EXPRewardType().register();
        loadExternalRewardTypes();
    }

    private void loadRequirementManager() {
        // Load RequirementManager
        RequirementManager.getInstance().load();
        getServer().getPluginManager().registerEvents(RequirementManager.getInstance(), this);

        // Load RequirementTypes
        new BiomeRequirementType().register();
        new BiomeSetRequirementType().register();
        new DisabledRequirementType().register();
        new InGameTimeRequirementType().register();
        new IRLTimeRequirementType().register();
        new MoonPhaseRequirementType().register();
        new NearbyPlayersRequirementType().register();
        new PermissionRequirementType().register();
        new RegionRequirementType().register();
        new WeatherRequirementType().register();
        new WorldRequirementType().register();

        // Load Group RequirementType
        if (isUsingVault()) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp != null) {
                new GroupRequirementType(rsp.getProvider()).register();
            }
        }
    }

    private void loadExternalRewardTypes() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.isPluginEnabled("PlayerPoints")) {
            new PlayerPointsRewardType().register();
        }
        if (pm.isPluginEnabled("GriefPrevention")) {
            new GPClaimBlocksRewardType().register();
        }
        if (pm.isPluginEnabled("AuraSkills")) {
            new AuraSkillsXPRewardType().register();
        }
        if (pm.isPluginEnabled("mcMMO")) {
            new McMMOXPRewardType().register();
        }
        // Only enable the PERMISSION type if Vault perms is found.
        if (getPermission() != null && getPermission().isEnabled()) {
            new PermissionRewardType().register();
        }
        // Only enable the Money RewardType is Vault is enabled.
        if (pm.isPluginEnabled("Vault")) {
            new MoneyRewardType().register();
        }
    }

    public List<Player> getVisibleOnlinePlayers() {
        return MainConfig.getInstance().shouldRespectVanish() ? VanishChecker.getVisibleOnlinePlayers() : new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    // FISH TOGGLE METHODS
    // We use Strings here because Spigot 1.16.5 does not have PersistentDataType.BOOLEAN.

    public void performFishToggle(@NotNull Player player) {
        NamespacedKey key = new NamespacedKey(this, "fish-enabled");
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        // If it is enabled, disable it
        if (isCustomFishing(player)) {
            pdc.set(key, PersistentDataType.STRING, "false");
            ConfigMessage.TOGGLE_OFF.getMessage().send(player);
        // If it is disabled, enable it
        } else {
            pdc.set(key, PersistentDataType.STRING, "true");
            ConfigMessage.TOGGLE_ON.getMessage().send(player);
        }
    }

    public boolean isCustomFishing(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(this, "fish-enabled");
        String toggleValue = pdc.getOrDefault(key, PersistentDataType.STRING, "true");
        return toggleValue.equals("true");
    }

    private static PlatformAdapter loadAdapter() {
        // Class names taken from PaperLib's initialize method
        if (FishUtils.classExists(("com.destroystokyo.paper.PaperConfig"))) {
            return new PaperAdapter();
        } else if (FishUtils.classExists("io.papermc.paper.configuration.Configuration")) {
            return new PaperAdapter();
        }
        return new SpigotAdapter();
    }

    public static @NotNull PlatformAdapter getAdapter() {
        if (platformAdapter == null) {
            instance.getLogger().warning("No PlatformAdapter found! Defaulting to SpigotAdapter.");
            platformAdapter = new SpigotAdapter();
        }
        return platformAdapter;
    }

}
