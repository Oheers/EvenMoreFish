package com.oheers.fish.selling;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class GUICache {

    public static ArrayList<SellGUI> guis;

    public GUICache() {
        guis = new ArrayList<>();
    }

    public static ArrayList<SellGUI> getGuis() {
        return guis;
    }

    public static boolean isOpenSellGUI(Player player) {
        for (SellGUI gui : guis) {
            if (gui.getPlayer().equals(player)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSellGUI(Inventory inv) {
        for (SellGUI gui : guis) {
            if (gui.getMenu().equals(inv)) {
                return true;
            }
        }

        return false;
    }
}
