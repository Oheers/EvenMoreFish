package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Sound;
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

            if (event.getClickedInventory() != null) {

                if (event.getView().getTopInventory() == event.getClickedInventory()) {

                    ItemStack clickedItem = event.getClickedInventory().getItem(event.getSlot());
                    if (clickedItem != null) {

                        // determines what the player has clicked, or if they've just added an item
                        // to the menu
                        if (clickedItem.isSimilar(gui.getSellIcon())) {
                            // cancels on right click
                            if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                                event.setCancelled(true);
                                gui.close(false);
                                return;
                            }

                            // makes the player confirm their choice
                            gui.createIcon();
                            gui.setIcon();
                            gui.setFiller();

                            gui.setModified(false);
                            event.setCancelled(true);

                        } else if (clickedItem.isSimilar(gui.getErrorIcon())) {
                            // cancels on right click
                            if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                                event.setCancelled(true);
                                gui.close(false);
                                return;
                            }

                            // makes the player confirm their choice
                            gui.createIcon();
                            gui.setIcon();
                            gui.setFiller();

                            gui.setModified(false);
                            event.setCancelled(true);

                        } else if (clickedItem.isSimilar(gui.getConfirmIcon())) {
                            // cancels on right click
                            if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                                event.setCancelled(true);
                                gui.close(false);
                                return;
                            }

                            // the player is at the join stage
                            event.setCancelled(true);
                            if (gui.getModified()) {

                                // the menu has been modified since we last gave the confirmation button, so it sends it again
                                gui.createIcon();
                                gui.setIcon();

                                gui.setModified(false);
                            } else {
                                gui.close(true);
                                EvenMoreFish.econ.depositPlayer(((Player) event.getWhoClicked()).getPlayer(), gui.value);

                                // sending the sell message to the player
                                Message msg = new Message((Player) event.getWhoClicked())
                                        .setMSG(EvenMoreFish.msgs.getSellMessage())
                                        .setSellPrice(Double.toString(gui.getSellPrice()))
                                        .setAmount(Integer.toString(gui.fishCount));
                                gui.getPlayer().sendMessage(msg.toString());
                                ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.06f);
                            }
                        } else if (clickedItem.isSimilar(gui.getFiller()) || clickedItem.isSimilar(gui.getErrorFiller())) {
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
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        GUICache.attemptPop((Player) event.getPlayer(), false);
    }
}
