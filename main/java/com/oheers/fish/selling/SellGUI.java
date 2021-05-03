package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SellGUI {

    private final Player player;

    private Inventory menu;

    public boolean modified;

    public double value;

    private boolean error;

    public int fishCount;

    public int guiSize;

    private ItemStack sellIcon, filler, errorFiller, confirmIcon, noValueIcon;

    public SellGUI(Player p) {
        this.guiSize = (EvenMoreFish.mainConfig.getGUISize()+1)*9;
        this.player = p;
        this.modified = false;
        makeMenu();
        setFiller();
        addFiller(filler);
        setSellItem();
        this.player.openInventory(menu);
    }

    private void makeMenu() {
        System.out.println(guiSize);
        System.out.println(EvenMoreFish.msgs.getWorthGUIName());
        this.menu = Bukkit.createInventory(null, guiSize, ChatColor.translateAlternateColorCodes('&', EvenMoreFish.msgs.getWorthGUIName()));
    }

    public Player getPlayer() {
        return player;
    }

    public void setFiller() {
        // the gray glass panes at the bottom
        ItemStack fill = new ItemStack(Material.valueOf(EvenMoreFish.mainConfig.getFiller()));
        ItemStack error = new ItemStack(Material.valueOf(EvenMoreFish.mainConfig.getFillerError()));
        ItemMeta fillMeta = fill.getItemMeta();
        ItemMeta errMeta = error.getItemMeta();
        fillMeta.setDisplayName(ChatColor.RESET + "");
        errMeta.setDisplayName(ChatColor.RESET + "");
        fill.setItemMeta(fillMeta);
        error.setItemMeta(errMeta);

        // sets it as a default menu item that won't be dropped in a .close() request
        this.filler = WorthNBT.attributeDefault(fill);
        this.errorFiller = WorthNBT.attributeDefault(error);
    }

    public void addFiller(ItemStack fill) {
        menu.setItem(guiSize-9, fill);
        menu.setItem(guiSize-8, fill);
        menu.setItem(guiSize-7, fill);
        menu.setItem(guiSize-6, fill);
        // Sell icon
        menu.setItem(guiSize-4, fill);
        menu.setItem(guiSize-3, fill);
        menu.setItem(guiSize-2, fill);
        menu.setItem(guiSize-1, fill);
    }

    public void setSellItem() {
        ItemStack sIcon = new ItemStack(Material.valueOf(EvenMoreFish.mainConfig.getSellItem()));
        ItemMeta sellMeta = sIcon.getItemMeta();
        sellMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', EvenMoreFish.msgs.getSellName()));
        // Generates the lore, looping through each line in messages.yml lore thingy, and generating it
        List<String> lore = new ArrayList<>();
        for (String line : EvenMoreFish.msgs.sellLore()) {
            lore.add(new Message(this.player).setMSG(line).setSellPrice(getTotalWorth()).toString());
        }
        sellMeta.setLore(lore);

        sIcon.setItemMeta(sellMeta);
        glowify(sIcon);

        this.sellIcon = WorthNBT.attributeDefault(sIcon);
        menu.setItem(guiSize-5, this.sellIcon);
    }

    public ItemStack getSellIcon() {
        return this.sellIcon;
    }

    public ItemStack getConfirmIcon() {
        return this.confirmIcon;
    }

    public ItemStack getErrorIcon() {
        return this.noValueIcon;
    }

    public void createIcon() {
        String totalWorth = getTotalWorth();
        if (totalWorth.equals("0.0")) {
            ItemStack error = new ItemStack(Material.valueOf(EvenMoreFish.mainConfig.getSellItemError()));
            ItemMeta errorMeta = error.getItemMeta();
            errorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', EvenMoreFish.msgs.getNoValueName()));
            List<String> lore = new ArrayList<>();
            for (String line : EvenMoreFish.msgs.noValueLore()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            errorMeta.setLore(lore);
            error.setItemMeta(errorMeta);
            glowify(error);
            this.noValueIcon = WorthNBT.attributeDefault(error);
            this.error = true;
        } else {
            ItemStack confirm = new ItemStack(Material.valueOf(EvenMoreFish.mainConfig.getSellItemConfirm()));
            ItemMeta cMeta = confirm.getItemMeta();
            cMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', EvenMoreFish.msgs.getConfirmName()));
            // Generates the lore, looping through each line in messages.yml lore thingy, and generating it
            List<String> lore = new ArrayList<>();
            for (String line : EvenMoreFish.msgs.sellLore()) {
                lore.add(new Message(this.player).setMSG(line).setSellPrice(totalWorth).toString());
            }
            cMeta.setLore(lore);

            confirm.setItemMeta(cMeta);
            glowify(confirm);
            this.confirmIcon = WorthNBT.attributeDefault(confirm);
            this.error = false;
        }
    }

    public void setIcon() {
        if (this.error) {
            this.menu.setItem(guiSize-5, null);
            this.menu.setItem(guiSize-5, this.noValueIcon);
            this.addFiller(errorFiller);
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 0.0f);
        } else {
            this.menu.setItem(guiSize-5, null);
            this.menu.setItem(guiSize-5, this.confirmIcon);
            this.addFiller(filler);
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.75f);
        }

    }

    public String getTotalWorth() {
        if (this.menu == null) return Double.toString(0.0d);

        double val = 0.0d;
        int count = 0;

        for (ItemStack is : this.menu.getContents()) {
            // -1.0 is given when there's no worth NBT value
            if (WorthNBT.getValue(is) != -1.0) {
                val += (WorthNBT.getValue(is) * is.getAmount());
                count += is.getAmount();
            }
        }

        this.value = val;
        this.fishCount = count;

        return Double.toString(val);
    }

    // will drop only non-fish items if the method is called from selling, and everything if it's just a gui close
    public void close(boolean selling) {
        EvenMoreFish.guis.remove(this);
        player.closeInventory();

        if (selling) rescueNonFish();
        else rescueAllItems();
    }

    public ItemStack getFiller() {
        return this.filler;
    }

    public ItemStack getErrorFiller() {
        return this.errorFiller;
    }

    public void setMenu(Inventory inv) {
        this.menu = inv;
    }

    public void setModified(boolean mod) {
        this.modified = mod;
    }

    public boolean getModified() {
        return this.modified;
    }

    public double getSellPrice() {
        return this.value;
    }

    // for each item in the menu, if it isn't a default menu item, it's dropped at the player's feet
    private void rescueAllItems() {
        List<ItemStack> throwing = new ArrayList<>();
        for (ItemStack i : this.menu) {
            if (i != null) {
                if (!WorthNBT.isDefault(i)) {
                    throwing.add(i);
                }
            }
        }
        FishUtils.giveItems(throwing, this.player);
    }

    private void rescueNonFish() {
        List<ItemStack> throwing = new ArrayList<>();
        for (ItemStack i : this.menu) {
            if (i != null) {
                if (!(WorthNBT.isDefault(i)) && !(FishUtils.isFish(i))) {
                    throwing.add(i);
                }
            }
        }
        FishUtils.giveItems(throwing, this.player);
    }

    private void glowify(ItemStack i) {

        // plops on the unbreaking 1 enchantment to make it glow
        i.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemMeta meta = i.getItemMeta();

        // hides the unbreaking 1 enchantment from showing in the lore
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        i.setItemMeta(meta);
    }
}
