package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;

public class InteractHandler implements Listener {

    @EventHandler
    public void interact(InventoryClickEvent event) {
        Inventory clicked = event.getInventory();
        if (GUICache.isSellGUI(clicked)) {
            if (event.getSlot() >= 27) {
                event.setCancelled(true);
            }
        }
    }
}
