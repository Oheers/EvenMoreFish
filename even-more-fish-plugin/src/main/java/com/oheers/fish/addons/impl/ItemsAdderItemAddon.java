package com.oheers.fish.addons.impl;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.ItemAddon;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderItemAddon extends ItemAddon implements Listener {
    private boolean itemsAdderLoaded = false;

    @Override
    public String getPrefix() {
        return "itemsadder";
    }

    @Override
    public String getPluginName() {
        return "ItemsAdder";
    }

    @Override
    public String getAuthor() {
        return "sarhatabaot";
    }

    @Override
    public ItemStack getItemStack(String id) {
        if (!itemsAdderLoaded) {
            return null;
        }

        String[] splitMaterialValue = id.split(":");
        if (splitMaterialValue.length != 3) {
            EvenMoreFish.logger.severe(() -> String.format("Incorrect format for ItemsAdderItemAddon, use %s:namespace:id",getPrefix()));
            return null;
        }

        final String namespaceId = splitMaterialValue[1] + ":" + splitMaterialValue[2];
        final CustomStack customStack = CustomStack.getInstance(namespaceId);
        if (customStack == null) {
            EvenMoreFish.logger.info(() -> String.format("Could not obtain itemsadder item %s", namespaceId));
            return null;
        }
        return CustomStack.getInstance(namespaceId).getItemStack();

    }

    @EventHandler
    public void onItemsLoad(ItemsAdderLoadDataEvent event) {
        EvenMoreFish.getInstance().getLogger().info("Detected that itemsadder has finished loading all items...");
        EvenMoreFish.getInstance().getLogger().info("Reloading EMF.");
        this.itemsAdderLoaded = true;
        EvenMoreFish.getInstance().reload();
    }
}
