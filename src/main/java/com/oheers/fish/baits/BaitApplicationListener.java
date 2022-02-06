package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BaitApplicationListener implements Listener {

	@EventHandler
	public void onClickEvent(InventoryClickEvent event) {

		ItemStack clickedItem;
		ItemStack cursor;

		if ((clickedItem = event.getCurrentItem()) == null) return;
		if ((cursor = event.getCursor()) == null) return;

		if (clickedItem.getType() == Material.FISHING_ROD) {
			if (BaitNBTManager.isBaitObject(event.getCursor())) {

				if (!event.getWhoClicked().getGameMode().equals(GameMode.SURVIVAL)) {
					event.getWhoClicked().sendMessage(FishUtils.translateHexColorCodes("&cYou must be in &nsurvival&c to apply baits to fishing rods."));
					return;
				}

				ItemStack completedItem;

				try {
					if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						completedItem = BaitNBTManager.applyBaitedRodNBT(clickedItem, BaitNBTManager.getBaitName(event.getCursor()), event.getCursor().getAmount());
					} else {
						completedItem = BaitNBTManager.applyBaitedRodNBT(clickedItem, BaitNBTManager.getBaitName(event.getCursor()), 1);
					}

				} catch (MaxBaitsReachedException exception) {
					event.getWhoClicked().sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getMaxBaitsReceived()));
					return;
				}

				if (completedItem == null) return;

				event.setCancelled(true);

				event.setCurrentItem(completedItem);

				if (cursor.getAmount() == 1 || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
				} else {
					cursor.setAmount(cursor.getAmount() - 1);
					event.getWhoClicked().setItemOnCursor(cursor);
				}
			}
		}
	}
}
