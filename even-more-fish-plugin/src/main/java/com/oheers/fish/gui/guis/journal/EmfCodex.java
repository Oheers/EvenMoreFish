package com.oheers.fish.gui.guis.journal;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmfCodex extends JavaPlugin {
    private DatabaseManager dbManager;
    private final Map<String, String> placeholderCache = new ConcurrentHashMap<>();

    public void updatePlaceholder(Player player, String identifier, String value) {
        String key = player.getUniqueId() + ":" + identifier;
        placeholderCache.put(key, value);
    }

    public String getPlaceholder(Player player, String identifier) {
        String key = player.getUniqueId() + ":" + identifier;
        return placeholderCache.getOrDefault(key, "checking...");
    }


    @Override
    public void onEnable() {
        // Save the default config.yml if it does not exist
        saveDefaultConfig();

        dbManager = new DatabaseManager();
        dbManager.connect(); // Initialize the database connection

        try {
            GUIConfig.init(this);
        } catch (IOException e) {
            getLogger().severe("Failed to load GUI configuration: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register the PlaceholderAPI expansion
        EmfCodexExpansion expansion = null;
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion = new EmfCodexExpansion(this);
            expansion.register();
        }

        getServer().getPluginManager().registerEvents(new FishCatchListener(dbManager), this);

        // Register the command executor and tab completer
        CodexCommandExecutor commandExecutor = new CodexCommandExecutor(dbManager, this);
        this.getCommand("opencodex").setExecutor(commandExecutor);
        this.getCommand("opencodex").setTabCompleter(commandExecutor);

        // Register the PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EmfCodexExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        dbManager.close();
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }
}