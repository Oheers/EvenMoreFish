package com.oheers.fish.competition.reward.gui;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIClose implements Listener {

    @EventHandler
    public void guiClose(InventoryCloseEvent event) {
        EvenMoreFish.rGuis.removeIf(rGui -> rGui.viewer == event.getPlayer());
    }
}
