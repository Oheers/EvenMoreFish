package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.api.EMFFishEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;

public class FishCatchListener implements Listener {
    private final DatabaseManager dbManager;

    public FishCatchListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onFishCatch(EMFFishEvent event) {
        Player player = event.getPlayer();
        String fishName = event.getFish().getName();
        String rarity = event.getFish().getRarity().getId();
        String rarityColour = event.getFish().getRarity().getColour();
        double size = event.getFish().getLength();
        String discoverer = player.getName();

        CompletableFuture<Double> serverBestSizeFuture = dbManager.getServerBestSize(fishName);
        CompletableFuture<Double> serverShortestSizeFuture = dbManager.getServerShortestSize(fishName);
        CompletableFuture<Integer> serverCaughtFuture = dbManager.getServerCaughtCount(fishName);

        CompletableFuture.allOf(serverBestSizeFuture, serverShortestSizeFuture, serverCaughtFuture).thenRun(() -> {
            try {
                double serverBestSize = serverBestSizeFuture.get();
                double serverShortestSize = serverShortestSizeFuture.get();
                int serverCaught = serverCaughtFuture.get() + 1; // Increment serverCaught

                dbManager.updateFishData(
                        player.getUniqueId(),
                        fishName,
                        rarity,
                        rarityColour,
                        size,
                        discoverer,
                        Math.max(serverBestSize, size), // Update serverBestSize if the new fish is larger
                        serverShortestSize == 0 ? size : Math.min(serverShortestSize, size), // Update serverShortestSize if the new fish is shorter
                        serverCaught
                ).join();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}