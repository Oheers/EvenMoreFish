package com.oheers.fish;

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
import com.oheers.fish.database.Database;
import com.oheers.fish.economy.GriefPreventionEconomyType;
import com.oheers.fish.economy.PlayerPointsEconomyType;
import com.oheers.fish.economy.VaultEconomyType;
import com.oheers.fish.events.*;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.requirements.*;
import com.oheers.fish.utils.AntiCraft;
import com.oheers.fish.utils.HeadDBIntegration;
import com.oheers.fish.utils.ItemFactory;
import com.oheers.fish.utils.nbt.NbtKeys;
import de.themoep.inventorygui.InventoryGui;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
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
    private boolean firstLoad = false;

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

    private Database database;
    private HeadDatabaseAPI HDBapi;

    private static EvenMoreFish instance;
    private static TaskScheduler scheduler;
    private static PlatformAdapter platformAdapter;
    private EMFAPI api;

    private AddonManager addonManager;
    private Map<String, String> commandUsages = new HashMap<>();

    public static EvenMoreFish getInstance() {
        return instance;
    }

    public static TaskScheduler getScheduler() {return scheduler;}

    public AddonManager getAddonManager() {
        return addonManager;
    }

    @Override
    public void onLoad() {
        CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
                .usePluginNamespace()
                .missingExecutorImplementationMessage("You are not able to use this command!");
        CommandAPI.onLoad(config);
    }

    @Override
    public void onEnable() {

        if (!NBT.preloadApi()) {
            throw new RuntimeException("NBT-API wasn't initialized properly, disabling the plugin");
        }

        // This should only ever be done once.
        EMFPlugin.setInstance(this);

        CommandAPI.onEnable();

        // If EMF folder does not exist, this is the first load.
        firstLoad = !getDataFolder().exists();

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
        registerCommands();

        if (!MainConfig.getInstance().debugSession()) {
            metrics();
        }

        AutoRunner.init();

        if (MainConfig.getInstance().databaseEnabled()) {
            DataManager.init();

            database = new Database();
            DataManager.getInstance().loadUserReportsIntoCache();
        }

        logger.log(Level.INFO, "EvenMoreFish by Oheers : Enabled");

        // Set this to false as the plugin is now loaded.
        firstLoad = false;
    }

    @Override
    public void onDisable() {

        // Prevent issues when NBT preload fails.
        if (instance == null) {
            return;
        }

        CommandAPI.onDisable();

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
            database.shutdown();
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
            getInstance().getLogger().log(level, () -> "DEBUG %s".formatted(message));
        }
    }

    public static void dbVerbose(final String message) {
        if (MainConfig.getInstance().doDBVerbose()) {
            getInstance().getLogger().info("DB-VERBOSE %s".formatted(message));
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

    public Map<String, String> getCommandUsages() {
        return commandUsages;
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

            DataManager.getInstance().saveFishReports();
            DataManager.getInstance().saveUserReports();

            DataManager.getInstance().uncacheAll();
        };
        if (scheduler) {
            getScheduler().runTask(save);
        } else {
            save.run();
        }
    }


    public ItemStack createCustomNBTRod() {
        ItemFactory itemFactory = new ItemFactory("nbt-rod-item", MainConfig.getInstance().getConfig());
        itemFactory.enableDefaultChecks();
        itemFactory.setItemDisplayNameCheck(true);
        itemFactory.setItemLoreCheck(true);

        ItemStack customRod = itemFactory.createItem(null, 0);

        setCustomNBTRod(customRod);

        return customRod;
    }

    /**
     * Allows external plugins to set their own items as an EMF NBT-rod.
     * @param item The item to set as an EMF NBT-rod.
     */
    public void setCustomNBTRod(@NotNull ItemStack item) {
        NBT.modify(item, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setBoolean(NbtKeys.EMF_ROD_NBT, true);
        });
    }

    public void reload(@Nullable CommandSender sender) {

        // If EMF folder does not exist, assume first load again.
        firstLoad = !getDataFolder().exists();

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

        firstLoad = false;

    }

    private void registerCommands() {
        new EMFCommand().getCommand().register(this);

        // Shortcut command for /emf admin
        if (MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            new AdminCommand(
                    MainConfig.getInstance().getAdminShortcutCommandName()
            ).getCommand().register(this);
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

    public Database getDatabase() {
        return database;
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

    public boolean isFirstLoad() {
        return firstLoad;
    }

}
