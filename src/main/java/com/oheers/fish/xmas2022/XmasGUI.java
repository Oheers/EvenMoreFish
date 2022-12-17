package com.oheers.fish.xmas2022;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.FishReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.selling.WorthNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class XmasGUI implements InventoryHolder {

    final Inventory inventory;
    final int INV_SIZE = 54;
    final UUID viewer;

    /**
     * Creates the advent calendar GUI for the player and loads all filler items in for them. The fish will also be
     * generated dependent on whether they have been unlocked yet or not.
     *
     * @param viewer The UUID of the player who will open the GUI.
     */
    public XmasGUI(@NotNull final UUID viewer) {
        this.inventory = Bukkit.createInventory(this, INV_SIZE, new Message(EvenMoreFish.xmas2022Config.getGUIName()).getRawMessage(true, false));
        this.viewer = viewer;
        loadFiller();
        setFish();
    }

    /**
     * Loads the inventory for the player and also changes it to be to the specification of the user (right now that's just
     * the state of /emf toggle.
     * @param player The player to send the inventory to.
     */
    public void display(Player player) {
        player.openInventory(this.inventory);
    }

    /**
     * Takes the initial filler ItemStack object and applies NBT tags to prevent them from being taken out, as well
     * as display tags that modify the displayname to set them to ""
     *
     * @param slot The slot number within the GUI
     * @param itemMaterial The material which will be given
     */
    private void setFillerItem(int slot, Material itemMaterial) {
        ItemStack fillerStack = new ItemStack(itemMaterial);
        ItemMeta fillerMeta = fillerStack.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(ChatColor.RESET + "");
            fillerStack.setItemMeta(fillerMeta);
            WorthNBT.attributeDefault(fillerStack);
        }
        this.inventory.setItem(slot, fillerStack);
    }

    /**
     * Loops through all empty slots left within the GUI and will fill them in with fish in order of the day they are
     * set to appear in. Lore, display-name and NBT data will all be applied in this method too.
     */
    public void setFish() {
        int day = 0;
        List<FishReport> fishReportList = DataManager.getInstance().getFishReportsIfExists(viewer);
        dayLoop:
            for (int i = 0; i < 54; i++) {
                if (this.inventory.getItem(i) != null) {
                    continue;
                }

                day++;

                try {
                    if (fishReportList == null) this.inventory.setItem(i, new ItemStack(Material.COD));

                    Fish currentDay = EvenMoreFish.xmasFish.get(day);
                    for (FishReport fishReport : fishReportList) {
                        if (fishReport.getName().equals(currentDay.getName()) && fishReport.getRarity().equals("Christmas 2022")) {
                            this.inventory.setItem(i, currentDay.give(-1));
                            continue dayLoop;
                        }
                    }
                    this.inventory.setItem(i, new ItemStack(Material.COD));

                } catch (NullPointerException exception) {
                    EvenMoreFish.logger.log(Level.SEVERE, "No fish found for day (" + day + ") in xmas2022.yml config file.");
                }
            }
    }

    public void loadFiller() {
        EvenMoreFish.xmas2022Config.fillerDefault.forEach(this::setFillerItem);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
