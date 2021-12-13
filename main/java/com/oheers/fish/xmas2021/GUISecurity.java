package com.oheers.fish.xmas2021;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUISecurity implements Listener {
    @EventHandler
    public void guiInteract(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof XmasFishGUI) {
            event.setCancelled(true);
        }
    }
}