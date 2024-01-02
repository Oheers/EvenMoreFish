package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Gui implements InventoryHolder {
    protected final Inventory inventory;
    protected final UUID viewer;


    public Gui(UUID viewer, Message title) {
        this.inventory = Bukkit.createInventory(this, getInvSize(), title.getRawMessage(true, false));;
        this.viewer = viewer;
    }

    public int getInvSize() {
        return 54;
    }

    public FillerStyle getFillerStyle() {
        return FillerStyle.DEFAULT;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Loads the inventory for the player and also changes it to be to the specification of the user (right now that's just
     * the state of /emf toggle.
     * @param player The player to send the inventory to.
     */
    public abstract void display(final Player player);

    public abstract void loadFiller();
}
