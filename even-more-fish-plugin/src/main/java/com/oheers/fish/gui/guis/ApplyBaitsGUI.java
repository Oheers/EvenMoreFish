package com.oheers.fish.gui.guis;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.ApplicationResult;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitManager;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.config.GUIFillerConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ApplyBaitsGUI implements EMFGUI {

    private final InventoryGui gui;
    private final Player player;
    private final Inventory baitInventory;

    public ApplyBaitsGUI(@NotNull Player player, @Nullable Inventory baitInventory) {
        this.player = player;

        Section section = GUIConfig.getInstance().getConfig().getSection("apply-baits-menu");

        this.baitInventory = Objects.requireNonNullElseGet(baitInventory, () -> Bukkit.createInventory(null, 54));

        this.gui = GUIUtils.createGUI(section);
        if (section == null) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not find the config for the Apply Baits GUI!");
            return;
        }

        gui.setFiller(GUIUtils.getFillerItem(section.getString("filler"), Material.GRAY_STAINED_GLASS_PANE));
        gui.addElements(GUIUtils.getElements(section, this, null));
        gui.addElements(GUIFillerConfig.getInstance().getDefaultFillerElements());

        gui.addElement(new GuiStorageElement(
                FishUtils.getCharFromString(section.getString("bait-character", "b"), 'b'),
                this.baitInventory
        ));

        gui.setCloseAction(close -> {
            processBaits();
            doRescue();
            return false;
        });
    }

    @Override
    public void doRescue() {
        GUIUtils.doRescue(this.baitInventory, this.player);
    }

    private void processBaits() {
        ItemStack handItem = this.player.getInventory().getItemInMainHand();
        if (!handItem.getType().equals(Material.FISHING_ROD)) {
            return;
        }
        boolean changedRod = false;
        List<String> ignoredBaits = new ArrayList<>();
        for (ItemStack item : baitInventory.getContents()) {
            Bait bait = BaitManager.getInstance().getBait(item);
            if (bait == null) {
                continue;
            }
            if (ignoredBaits.contains(bait.getName())) {
                continue;
            }
            ApplicationResult result;

            // Try to apply all the baits.
            try {
                result = BaitNBTManager.applyBaitedRodNBT(handItem, bait, item.getAmount());
                EvenMoreFish.getInstance().incrementMetricBaitsApplied(item.getAmount());
            // When a specific bait is maxed.
            } catch (MaxBaitReachedException exception) {
                AbstractMessage message = ConfigMessage.BAITS_MAXED_ON_ROD.getMessage();
                message.setBaitTheme(bait.getTheme());
                message.setBait(bait.getName());
                message.send(this.player);
                // We should now start to ignore this bait.
                ignoredBaits.add(bait.getName());
                continue;
            // When the rod cannot contain any more baits.
            } catch (MaxBaitsReachedException exception) {
                ConfigMessage.BAITS_MAXED.getMessage().send(this.player);
                // Return here as the fishing rod cannot fit any more baits.
                return;
            }

            if (result == null || result.getFishingRod() == null) {
                continue;
            }

            // Remove the bait items from the inventory.
            this.baitInventory.remove(item);
            // Set the handItem variable.
            handItem = result.getFishingRod();
            changedRod = true;
        }

        if (changedRod) {
            this.player.getInventory().setItemInMainHand(handItem);
        }
    }

    @Override
    public InventoryGui getGui() {
        return this.gui;
    }

    @Override
    public void open() {
        gui.show(this.player);
    }

}
