package com.oheers.fish.competition.reward.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RewardGUI {

    Inventory inv;
    Player viewer;

    public RewardGUI(Player viewer) {
        this.inv = Bukkit.createInventory(null, 18, "RewardGUI");
        this.viewer = viewer;
    }

    // Opens the inventory to the player. The GUI must have been constructed beforehand
    public void display() {
        this.viewer.openInventory(this.inv);
    }
}
