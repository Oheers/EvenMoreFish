package com.oheers.fish.gui.guis.journal;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EmfCodexExpansion extends PlaceholderExpansion implements Taskable {

    private final EmfCodex plugin;
    private final DatabaseCache databaseCache;
    private final DatabaseManager dbManager;

    public EmfCodexExpansion(EmfCodex plugin) {
        this.plugin = plugin;
        this.databaseCache = new DatabaseCache(plugin.getDatabaseManager());
        this.dbManager = plugin.getDatabaseManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emfcodex";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.startsWith("found_")) {
            String rarity = identifier.substring(6);
            UUID playerId = player.getUniqueId();
            boolean hasShowAllFishPermission = player.hasPermission("emfcodex.showallfish");
            return databaseCache.hasCaughtFishOfRarity(playerId, rarity, hasShowAllFishPermission).join() ? "yes" : "no";
        }

        return null;
    }

    @Override
    public void start() {
        // Schedule the task to run periodically
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateCache, 0L, 20L); // Run every second
    }

    @Override
    public void stop() {
        // Cancel the scheduled task
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    public void updateCache() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            boolean hasShowAllFishPermission = player.hasPermission("emfcodex.showallfish");

            // Fetch data for each rarity and update the cache
            for (String rarity : FishData.getAllRarities()) {
                dbManager.hasCaughtFishOfRarity(playerId, rarity).thenAccept(found -> {
                    databaseCache.updateCache(playerId, rarity, found || hasShowAllFishPermission);
                });
            }
        }
    }

    public void updateCacheForPlayerAndRarity(Player player, String rarity) {
        UUID playerId = player.getUniqueId();
        boolean hasShowAllFishPermission = player.hasPermission("emfcodex.showallfish");
        dbManager.hasCaughtFishOfRarity(playerId, rarity).thenAccept(found -> {
            databaseCache.updateCache(playerId, rarity, found || hasShowAllFishPermission);
        });
    }
}