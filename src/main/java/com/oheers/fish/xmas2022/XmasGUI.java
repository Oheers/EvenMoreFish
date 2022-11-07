package com.oheers.fish.xmas2022;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

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
        this.inventory = Bukkit.createInventory(this, INV_SIZE, new Message(EvenMoreFish.xmas2022Config.getGUIName()).getRawMessage(true, false));
        loadFiller();
        this.viewer = viewer;
    }

    /**
     * Loads the inventory for the player and also changes it to be to the specification of the user (right now that's just
     * the state of /emf toggle.
     * @param player The player to send the inventory to.
     */
    public void display(Player player) {
        player.openInventory(this.inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void loadFiller() {
        EvenMoreFish.xmas2022Config.fillerDefault.forEach(this.inventory::setItem);
    }
}
