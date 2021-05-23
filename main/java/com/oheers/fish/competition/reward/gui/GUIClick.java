package com.oheers.fish.competition.reward.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIClick implements Listener {

    @EventHandler
    public void onInteract(InventoryClickEvent event) {
        for (RewardGUI rGui : EvenMoreFish.rGuis) {
            if (rGui.viewer == event.getWhoClicked()) {
                event.setCancelled(true);
                ItemStack clicked = event.getClickedInventory().getItem(event.getSlot());
                if (clicked != null) {
                    if (clicked.getType() == Material.SPECTRAL_ARROW) {
                        if (FishUtils.getScrollDirection(clicked)) rGui.setPage(rGui.getPage()+1);
                        else rGui.setPage(rGui.getPage()-1);
                        rGui.init();
                    }
                }
            }
        }
    }
}
