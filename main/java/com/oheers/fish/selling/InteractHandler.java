package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class InteractHandler implements Listener {

    @EventHandler
    public void interact(InventoryClickEvent event) {
        // is the player viewing a SellGUI gui?
        if (GUICache.isOpenSellGUI((Player) event.getWhoClicked())) {

            SellGUI gui = GUICache.getSellGUI((Player) event.getWhoClicked());
            // Updates the stored menu in "gui"
            gui.setMenu(event.getView().getTopInventory());

            ItemStack clickedItem = event.getClickedInventory().getItem(event.getSlot());
            if (clickedItem != null) {

                // determines what the player has clicked, or if they've just added an item
                // to the menu
                if (clickedItem.isSimilar(gui.getSellIcon())) {
                    // cancels on right click
                    if (event.getAction().equals(InventoryAction.PICKUP_HALF)) gui.close();

                    // makes the player confirm their choice
                    gui.createConfirmIcon();
                    gui.setConfirmIcon();

                    gui.setModified(false);
                    event.setCancelled(true);

                } else if (clickedItem.isSimilar(gui.getConfirmIcon())) {
                    // cancels on right click
                    if (event.getAction().equals(InventoryAction.PICKUP_HALF)) gui.close();

                    // the player is at the join stage
                    event.setCancelled(true);
                    if (gui.getModified()) {

                        // the menu has been modified since we last gave the confirmation button, so it sends it again
                        gui.createConfirmIcon();
                        gui.setConfirmIcon();

                        gui.setModified(false);
                    } else {
                        gui.close();
                        // do all the selling stuff
                    }
                } else if (clickedItem.isSimilar(gui.getFiller())) {
                    event.setCancelled(true);
                } else {
                    gui.setSellItem();
                    gui.setModified(true);
                }
            } else {
                gui.setSellItem();
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        GUICache.attemptPop((Player) event.getPlayer());
    }
}
