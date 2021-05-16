package com.oheers.fish.competition.reward.gui;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardGUI {

    Inventory inv;
    Player viewer;
    int page;

    public RewardGUI(Player viewer) {
        this.inv = Bukkit.createInventory(null, 18, "RewardGUI");
        this.viewer = viewer;
        this.page = 1;
        init(this.page);
    }

    private void init(int page) {
        // The amount of reward markers to be shown
        int quant = EvenMoreFish.rewards.size();

        // detects if all the slots in that page are filled or not
        if (page*9 > quant) {
            // used to neaten up the values in the gui by putting the last uneven number on the bottom row
            if (quant%2 != 0) {
                // formatted: for(centre alignment; how many to go along by; incrementing by 1)
                for (int i= (9-quant)/2; i-((9-quant)/2) < quant; i++) {
                    genItem(Integer.toString(i), i);
                }
            } else {
                // checks if values actually exist since "0" is even according to java
                if (quant != 0) {
                    // using 10 over 9 since there's one item in the bottom row that "quant" is still accounting for
                    for (int i= (10-quant)/2; i-((10-quant)/2) < quant-1; i++) {
                        genItem(Integer.toString(i), i);
                    }
                    genItem("13", 13);
                }
            }
        } else {
            for (int i=0; i<9; i++) {
                genItem("x: " + i, i);
            }
        }
    }

    // Just creating a default item now that shows slot id
    private void genItem(String name, int slot) {
        ItemStack it = new ItemStack(Material.ALLIUM);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        inv.setItem(slot, it);
    }

    // Opens the inventory to the player. The GUI must have been constructed beforehand
    public void display() {
        this.viewer.openInventory(this.inv);
    }
}
