package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractHandler implements Listener {

    EvenMoreFish emf;

    public InteractHandler(EvenMoreFish emf) {
        this.emf = emf;
    }

    @EventHandler
    public void interact(InventoryClickEvent event) {
        // is the player viewing a SellGUI gui?
        if (GUICache.isOpenSellGUI((Player) event.getWhoClicked())) {

            SellGUI gui = GUICache.getSellGUI((Player) event.getWhoClicked());
            // Updates the stored menu in "gui"
            gui.setMenu(event.getView().getTopInventory());

            if (event.getClickedInventory() != null) {

                ItemStack clickedItem = event.getClickedInventory().getItem(event.getSlot());
                if (clickedItem != null) {

                    // determines what the player has clicked, or if they've just added an item
                    // to the menu
                    if (clickedItem.isSimilar(gui.getSellIcon())) {
                        // cancels on right click
                        if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                            event.setCancelled(true);
                            gui.close();
                            gui.doRescue(false);
                            return;
                        }

                        // makes the player confirm their choice
                        gui.createIcon(false);
                        gui.setIcon(false);

                        gui.setModified(false);
                        event.setCancelled(true);

                    } else if (clickedItem.isSimilar(gui.getSellAllIcon())) {
                        gui.createIcon(true);
                        gui.setIcon(true);

                        gui.setModified(false);
                        event.setCancelled(true);

                    } else if (clickedItem.isSimilar(gui.getErrorIcon())) {
                        // cancels on right click
                        if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                            event.setCancelled(true);
                            gui.close();
                            gui.doRescue(false);
                            return;
                        }

                        // makes the player confirm their choice
                        gui.createIcon(false);
                        gui.setIcon(false);

                        gui.setModified(false);
                        event.setCancelled(true);
                    } else if (clickedItem.isSimilar(gui.getSellAllErrorIcon())) {

                        // makes the player confirm their choice
                        gui.createIcon(true);
                        gui.setIcon(true);

                        gui.setModified(false);
                        event.setCancelled(true);

                    } else if (clickedItem.isSimilar(gui.getConfirmSellAllIcon())) {

                            gui.sell(true);
                            gui.close();
                            gui.doRescue(false);

                    } else if (clickedItem.isSimilar(gui.getConfirmIcon())) {
                        // cancels on right click
                        if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                            event.setCancelled(true);
                            gui.close();
                            gui.doRescue(false);
                            return;
                        }

                        // the player is at the join stage
                        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) event.setCancelled(true);
                        event.setCancelled(true);
                        if (gui.getModified()) {

                            // the menu has been modified since we last gave the confirmation button, so it sends it again
                            gui.createIcon(event.getSlot() != EvenMoreFish.mainConfig.getSellSlot());
                            gui.setIcon(false);

                            gui.setModified(false);
                        } else {
                            gui.sell(false);
                            gui.close();
                            gui.doRescue(true);
                        }
                    } else if (clickedItem.isSimilar(gui.getFiller()) || clickedItem.isSimilar(gui.getErrorFiller())) {
                        event.setCancelled(true);
                    } else {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                gui.updateSellItem();
                                gui.updateSellAllItem();
                                gui.setModified(true);

                                gui.error = false;
                            }
                        }.runTaskLaterAsynchronously(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), 1);
                    }
                } else {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            gui.setSellItem();
                            gui.setSellAllItem();
                        }
                    }.runTaskLaterAsynchronously(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), 1);
                }
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        GUICache.attemptPop((Player) event.getPlayer(), false);
    }
}
