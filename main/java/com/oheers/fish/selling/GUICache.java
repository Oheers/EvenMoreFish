package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class GUICache {

    public static boolean isOpenSellGUI(Player player) {
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getPlayer().equals(player)) {
                return true;
            }
        }

        return false;
    }

    public static SellGUI getSellGUI(Player player) {
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getPlayer().equals(player)) {
                return gui;
            }
        }

        return null;
    }

    public static void attemptPop(Player player) {
        System.out.println("!: " + EvenMoreFish.guis.toString());
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getPlayer().equals(player)) {
                EvenMoreFish.guis.remove(gui);
                gui.close();
                return;
            }
        }
    }

    public static boolean isSellGUI(Inventory inv) {
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getMenu().equals(inv)) {
                return true;
            }
        }

        return false;
    }
}
