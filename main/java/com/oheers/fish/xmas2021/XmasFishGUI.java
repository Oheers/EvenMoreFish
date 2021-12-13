package com.oheers.fish.xmas2021;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class XmasFishGUI implements InventoryHolder {
    private final Inventory inventory;

    public XmasFishGUI() {
        this.inventory = Bukkit.createInventory(this, 54, FishUtils.translateHexColorCodes(FishUtils.translateHexColorCodes(EvenMoreFish.xmas2021Config.getGUIName())));
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
