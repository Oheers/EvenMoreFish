package com.oheers.fish.xmas2022;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.Xmas2022Config;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
        this.inventory = Bukkit.createInventory(this, INV_SIZE, new Message(Xmas2022Config.getInstance().getGUIName()).getRawMessage(true, false));
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
                    if (fishReportList == null) this.inventory.setItem(i, new ItemStack(Xmas2022Config.getInstance().getLockedFishMaterial()));

                    Fish currentDay = EvenMoreFish.getInstance().getXmasFish().get(day);
                    for (FishReport fishReport : fishReportList) {
                        if (fishReport.getName().equals(currentDay.getName()) && fishReport.getRarity().equals("Christmas 2022")) {
                            this.inventory.setItem(i, createItem(true, day, currentDay, fishReport));
                            continue dayLoop;
                        }
                    }
                    this.inventory.setItem(i, createItem(false, day, currentDay, null));

                } catch (NullPointerException exception) {
                    EvenMoreFish.getInstance().getLogger().severe("No fish found for day (" + day + ") in xmas2022.yml config file.");
                }
            }
    }

    public void loadFiller() {
        Xmas2022Config.getInstance().fillerDefault.forEach(this::setFillerItem);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private ItemStack createItem(final boolean unlocked, final int day, @NotNull final Fish fish, final FishReport fishReport) {
        if (unlocked) {
            ItemStack itemStack = EvenMoreFish.getInstance().getXmasFish().get(day).give(-1);
            ItemMeta meta = itemStack.getItemMeta();
            Message fishName = new Message(Xmas2022Config.getInstance().getFoundFishName());
            fishName.setDay(Integer.toString(day));
            fishName.setName(fish.getName());
            meta.setDisplayName(fishName.getRawMessage(true, true));
            Message fishLore = new Message(Xmas2022Config.getInstance().getFoundFishLore());
            fishLore.setName(fish.getName());
            fishLore.setNumCaught(Integer.toString(fishReport.getNumCaught()));
            fishLore.setLargestSize(Float.toString(fishReport.getLargestLength()));
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(fishReport.getTimeEpoch(), 0, ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale.ENGLISH);
            fishLore.setFirstCaught(dateTime.format(formatter));
            meta.setLore(fishLore.getRawListMessage(true, true));
            itemStack.setItemMeta(meta);
            return itemStack;
        } else {
            ItemStack itemStack = new ItemStack(Xmas2022Config.getInstance().getLockedFishMaterial());
            ItemMeta meta = itemStack.getItemMeta();
            Message lockedName = new Message(Xmas2022Config.getInstance().getLockedFishName());
            lockedName.setDay(Integer.toString(day));
            meta.setDisplayName(lockedName.getRawMessage(true, true));
            Message lockedLore = new Message(Xmas2022Config.getInstance().getLockedFishLore());
            lockedLore.setTimeRemaining("(never)");
            meta.setLore(lockedLore.getRawListMessage(true, true));
            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }
}
