package com.oheers.fish.gui.guis.journal;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class FishData {
    private final String fishName;
    private final String rarity;
    private final String rarityColour;
    private final int timesCaught;
    private final double largestSize;
    private final double shortestSize;
    private final String discoverDate;
    private final String discoverer;
    private final double serverBestSize;
    private final double serverShortestSize;
    private final int serverCaught;

    public FishData(String fishName, String rarity, String rarityColour, int timesCaught, double largestSize, double shortestSize, String discoverDate, String discoverer, double serverBestSize, double serverShortestSize, int serverCaught) {
        this.fishName = fishName;
        this.rarity = rarity;
        this.rarityColour = rarityColour;
        this.timesCaught = timesCaught;
        this.largestSize = largestSize;
        this.shortestSize = shortestSize;
        this.discoverDate = discoverDate;
        this.discoverer = discoverer;
        this.serverBestSize = serverBestSize;
        this.serverShortestSize = serverShortestSize;
        this.serverCaught = serverCaught;
    }

    public String getFishName() {
        return fishName;
    }

    public String getRarity() {
        return rarity;
    }

    public String getColour() {
        return FishUtils.translateColorCodes(rarityColour);
    }
    public int getTimesCaught() {
        return timesCaught;
    }

    public double getLargestSize() {
        return largestSize;
    }

    public double getShortestSize() {
        return shortestSize;
    }

    public String getDiscoverDate() {
        return discoverDate;
    }

    public String getDiscoverer() {
        return discoverer;
    }

    public double getServerBestSize() {
        return serverBestSize;
    }

    public double getServerShortestSize() {
        return serverShortestSize;
    }

    public int getServerCaught() {
        return serverCaught;
    }

    public ItemStack getFishItem(String fishName, String rarity) {
        fishName = fishName.trim();
        rarity = rarity.trim();

        // Find the correct Rarity object from the collection
        Rarity rarityObj = FishManager.getInstance().getRarityMap().get(rarity);

        if (rarityObj == null) {
            return new ItemStack(Material.COD);
        }

        for (Fish fish : rarityObj.getFishList()) { // Assuming Rarity class has getFishList method
            if (fish.getName().equalsIgnoreCase(fishName)) {
                return fish.getFactory().createItem(null, -1);
            }
        }

        return new ItemStack(Material.COD);
    }

    public static List<String> getAllRarities() {
        return FishManager.getInstance().getRarityMap().values().stream()
                .map(Rarity::getId)
                .collect(Collectors.toList());
    }
}