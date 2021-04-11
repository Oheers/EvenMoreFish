package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.entity.Player;

public class GUICache {

    // does the gui register contain the player "player"
    public static boolean isOpenSellGUI(Player player) {
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getPlayer().equals(player)) {
                return true;
            }
        }

        return false;
    }

    // once it's confirmed the player has a gui in the register, this returns the SellGUI object
    public static SellGUI getSellGUI(Player player) {
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getPlayer().equals(player)) return gui;
        }

        return null;
    }

    // attempts to remove the player's gui from the register and perform gui.close();
    public static void attemptPop(Player player) {
        for (SellGUI gui : EvenMoreFish.guis) {
            if (gui.getPlayer().equals(player)) {
                EvenMoreFish.guis.remove(gui);
                gui.close();
                return;
            }
        }
    }
}
