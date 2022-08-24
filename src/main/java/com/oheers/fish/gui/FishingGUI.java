package com.oheers.fish.gui;

import com.oheers.fish.config.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class FishingGUI implements InventoryHolder {

    final Inventory inventory;
    final int INV_SIZE = 45;

    /**
     * Creates a fishing GUI object to render the "/emf" gui. A new one needs to be made for each user otherwise it can
     * cause a mess with the /emf toggle button where the button will change for everyone if one person changes it. Essentially,
     * just make a new object for each user.
     */
    public FishingGUI() {
        this.inventory = Bukkit.createInventory(this, INV_SIZE, new Message("&1&lEvenMoreFish").getRawMessage(true, false));
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
}
