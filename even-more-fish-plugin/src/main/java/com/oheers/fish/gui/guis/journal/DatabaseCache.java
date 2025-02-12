package com.oheers.fish.gui.guis.journal;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseCache {
    private final DatabaseManager dbManager;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public DatabaseCache(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public CompletableFuture<Boolean> hasCaughtFishOfRarity(UUID playerId, String rarity, boolean hasShowAllFishPermission) {
        String key = playerId + "_" + rarity;
        if (hasShowAllFishPermission) {
            return CompletableFuture.completedFuture(true);
        }
        if (cache.containsKey(key)) {
            return CompletableFuture.completedFuture(Boolean.parseBoolean(cache.get(key)));
        } else {
            return dbManager.hasCaughtFishOfRarity(playerId, rarity).thenApply(found -> {
                cache.put(key, String.valueOf(found));
                return found;
            });
        }
    }

    public void updateCache(UUID playerId, String rarity, boolean found) {
        String key = playerId + "_" + rarity;
        cache.put(key, String.valueOf(found));
    }
}