package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.MaxBaitReachedException;
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
					event.getWhoClicked().sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getSurvivalOnly()));
					return;
				}

				ApplicationResult result = null;
				Bait bait = EvenMoreFish.baits.get(BaitNBTManager.getBaitName(event.getCursor()));

				try {
					if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						result = BaitNBTManager.applyBaitedRodNBT(clickedItem, bait, event.getCursor().getAmount());
					} else {
						result = BaitNBTManager.applyBaitedRodNBT(clickedItem, bait, 1);
					}

				} catch (MaxBaitsReachedException exception) {
					event.getWhoClicked().sendMessage(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getMaxBaitsReceived()));
					return;
				} catch (MaxBaitReachedException exception) {
					event.getWhoClicked().sendMessage(new Message()
							.setMSG(EvenMoreFish.msgs.getMaxBaitReceived())
							.setBaitTheme(bait.getTheme())
							.setBait(bait.getName())
							.toString());
				}

				if (result == null || result.getFishingRod() == null) return;

				event.setCancelled(true);
				event.setCurrentItem(result.getFishingRod());

				int cursorModifier = result.getCursorItemModifier();

				if (cursor.getAmount() - cursorModifier == 0) {
					event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
				} else {
					cursor.setAmount(cursor.getAmount() + cursorModifier);
					event.getWhoClicked().setItemOnCursor(cursor);
				}
			}
		}
	}
}
