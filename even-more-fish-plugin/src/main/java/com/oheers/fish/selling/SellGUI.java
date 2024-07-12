package com.oheers.fish.selling;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.utils.GUIUtils;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class SellGUI {

    private final InventoryGui gui;
    private final Player player;
    private final Inventory fishInventory;
    private MyScheduledTask task;

    public SellGUI(@NotNull Player player) {
        this.player = player;
        ConfigurationSection section = GUIConfig.getInstance().getConfig().getConfigurationSection("sell-menu");
        this.fishInventory = Bukkit.createInventory(null, 54);
        gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Sell Menu GUI!");
            return;
        }
        // Add filler and configured elements
        gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.GRAY_STAINED_GLASS_PANE));
        gui.addElements(GUIUtils.getElements(section));
        gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());

        // Create the Inventory and Element for the fish to be processed in
        gui.addElement(new GuiStorageElement(FishUtils.getCharFromString(section.getString("deposit-character"), 'i'), fishInventory));

        gui.setCloseAction(close -> {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (MainConfig.getInstance().sellOverDrop()) {
                SellHelper.sellInventoryGui(close.getGui(), close.getPlayer());
            }
            doRescue();
            return false;
        });
    }

    public void open() {
        gui.show(player);
        // Only start the task when the GUI is opened
        task = EvenMoreFish.getScheduler().runTaskTimer(gui::draw, 5L, 5L);
    }

    public Player getPlayer() {
        return player;
    }

    // will drop only non-fish items if the method is called from selling, and everything if it's just a gui close
    public void close() {
        gui.close();
    }

    // for each item in the menu, if it isn't a default menu item, it's dropped at the player's feet
    public void doRescue() {
        List<ItemStack> throwing = new ArrayList<>();
        for (ItemStack i : this.fishInventory.getContents()) {
            if (i != null) {
                if (!WorthNBT.isDefault(i)) {
                    throwing.add(i);
                }
            }
        }
        FishUtils.giveItems(throwing, this.player);
    }

}
