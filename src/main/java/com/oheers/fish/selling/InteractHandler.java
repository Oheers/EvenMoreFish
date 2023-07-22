package com.oheers.fish.selling;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.xmas2022.XmasGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractHandler implements Listener {

    EvenMoreFish emf;

    public InteractHandler(EvenMoreFish emf) {
        this.emf = emf;
    }

    @EventHandler
    public void interact(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof XmasGUI) {
            // Letting users still move items in their inventory around, because why not.
            if (event.getRawSlot() <= 53) event.setCancelled(true);
        }

        // is the player viewing a SellGUI gui?
        if (!(holder instanceof SellGUI)) {
            return;
        }
        SellGUI gui = (SellGUI) holder;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            new UniversalRunnable() {
                @Override
                public void run() {
                    gui.setSellItem();
                    gui.setSellAllItem();
                }
            }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(getClass()));
        } else {
            // determines what the player has clicked, or if they've just added an item
            // to the menu
            if (clickedItem.isSimilar(gui.getSellIcon()) || clickedItem.isSimilar(gui.getErrorIcon())) {
                // cancels on right click
                if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                    event.setCancelled(true);
                    gui.close();
                    return;
                }

                // makes the player confirm their choice
                gui.createIcon(false);
                gui.setIcon(false);

                gui.setModified(false);
                event.setCancelled(true);

            } else if (clickedItem.isSimilar(gui.getSellAllIcon()) || clickedItem.isSimilar(gui.getSellAllErrorIcon())) {
                gui.createIcon(true);
                gui.setIcon(true);

                gui.setModified(false);
                event.setCancelled(true);

            } else if (clickedItem.isSimilar(gui.getConfirmSellAllIcon())) {
                gui.sell(true);
                gui.close();
            } else if (clickedItem.isSimilar(gui.getConfirmIcon())) {
                // cancels on right click
                if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
                    event.setCancelled(true);
                    gui.close();
                    return;
                }

                event.setCancelled(true);
                if (gui.getModified()) {

                    // the menu has been modified since we last gave the confirmation button, so it sends it again
                    gui.createIcon(event.getSlot() != EvenMoreFish.mainConfig.getSellSlot());
                    gui.setIcon(false);

                    gui.setModified(false);
                } else {
                    gui.sell(false);
                    gui.close();
                }
            } else if (clickedItem.isSimilar(gui.getFiller()) || clickedItem.isSimilar(gui.getErrorFiller())) {
                event.setCancelled(true);
            } else {
                new UniversalRunnable() {
                    @Override
                    public void run() {
                        gui.updateSellItem();
                        gui.updateSellAllItem();
                        gui.setModified(true);
                        gui.resetGlassColour();

                        gui.error = false;
                    }
                }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(getClass()));
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof SellGUI)) {
            return;
        }
        SellGUI gui = (SellGUI) holder;
        if (EvenMoreFish.mainConfig.sellOverDrop()) {
            gui.sell(false);
        }
        gui.doRescue();
    }
}
