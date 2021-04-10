package com.oheers.fish.selling;

import com.oheers.fish.config.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellGUI {

    private final Player player;

    private Inventory menu;

    public SellGUI(Player p) {
        this.player = p;
        makeMenu();
        addFiller();
        addSellItem();
        this.player.openInventory(menu);
    }

    private void makeMenu() {
        menu = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', Messages.worthGUIName));
    }

    public Player getPlayer() {
        return player;
    }

    public void addFiller() {
        menu.setItem(27, new ItemStack(Material.ALLIUM));
    }

    public void addSellItem() {
        menu.setItem(28, new ItemStack(Material.ACACIA_WOOD));
    }

    public Inventory getMenu() {
        return menu;
    }
}
