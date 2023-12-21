package com.oheers.evenmorefish.addons;


import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.plugin.EMFPlugin;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

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
        if (splitMaterialValue.length != 2) {
            getLogger().severe(() -> String.format("Incorrect format for ItemsAdderItemAddon, use %s:namespace:id. Got %s",getPrefix(), id));
            return null;
        }

        final String namespaceId = splitMaterialValue[0] + ":" + splitMaterialValue[1];
        final CustomStack customStack = CustomStack.getInstance(namespaceId);
        if (customStack == null) {
            getLogger().info(() -> String.format("Could not obtain itemsadder item %s", namespaceId));
            return null;
        }
        return CustomStack.getInstance(namespaceId).getItemStack();

    }

    @EventHandler
    public void onItemsLoad(ItemsAdderLoadDataEvent event) {
        getLogger().info("Detected that itemsadder has finished loading all items...");
        getLogger().info("Reloading EMF.");
        this.itemsAdderLoaded = true;

        ((EMFPlugin) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("EvenMoreFish"))).reload();
    }
}
