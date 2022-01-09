package com.oheers.fish.baits;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BaitApplicationListener implements Listener {

	@EventHandler
	public void onClickEvent(InventoryClickEvent event) {

		if (event.getAction() == InventoryAction.PLACE_ALL) {
			Inventory inventory;
			ItemStack clickedItem;

			if ((inventory = event.getClickedInventory()) == null) return;
			if ((clickedItem = inventory.getItem(event.getSlot())) == null) return;

			if (clickedItem.getType() == Material.FISHING_ROD) {
				// bait may have been applied
			}
		}
	}
}
