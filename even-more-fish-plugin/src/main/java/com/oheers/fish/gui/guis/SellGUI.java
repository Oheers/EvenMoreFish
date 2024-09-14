package com.oheers.fish.gui.guis;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.selling.SellHelper;
import com.oheers.fish.selling.WorthNBT;
import com.oheers.fish.gui.GUIUtils;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class SellGUI implements EMFGUI {

    private final InventoryGui gui;
    private final Player player;
    private final Inventory fishInventory;
    private MyScheduledTask task;
    private final SellState sellState;

    public SellGUI(@NotNull Player player, @NotNull SellState sellState, @Nullable Inventory fishInventory) {
        this.sellState = sellState;
        this.player = player;
        Section section;
        if (sellState == SellState.NORMAL) {
            section = GUIConfig.getInstance().getConfig().getSection("sell-menu-normal");
        } else {
            section = GUIConfig.getInstance().getConfig().getSection("sell-menu-confirm");
        }
        this.fishInventory = Objects.requireNonNullElseGet(fishInventory, () -> Bukkit.createInventory(null, 54));
        gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Sell Menu GUI!");
            return;
        }
        // Add filler and configured elements
        gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.GRAY_STAINED_GLASS_PANE));
        gui.addElements(GUIUtils.getElements(section, this, () -> {
            Map<String, String> replacements = new HashMap<>();
            SellHelper playerHelper = new SellHelper(player.getInventory(), player);
            SellHelper shopHelper = new SellHelper(this.fishInventory, player);
            replacements.put("{sell-price}", String.valueOf(shopHelper.formatWorth(shopHelper.getTotalWorth())));
            replacements.put("{sell-all-price}", String.valueOf(playerHelper.formatWorth(playerHelper.getTotalWorth())));
            return replacements;
        }));
        gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());

        // Create the Inventory and Element for the fish to be processed in
        gui.addElement(new GuiStorageElement(FishUtils.getCharFromString(section.getString("deposit-character", "i"), 'i'), this.fishInventory));

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

    @Override
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

    @Override
    public InventoryGui getGui() {
        return this.gui;
    }

    public Inventory getFishInventory() {
        return fishInventory;
    }

    public enum SellState {
        NORMAL,
        CONFIRM
    }

}
