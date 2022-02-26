package com.oheers.fish.baits;

import org.bukkit.inventory.ItemStack;

public class ApplicationResult {

	ItemStack fishingRod;
	int cursorItemModifier;

	/**
	 * This is returned when a bait has been applied to a fishing rod, the remaining cursor items is how many of the bait
	 * were unsuccessfully applied, whether this was due to the fishing rod having no available slots, having maxed
	 * out the slot for that bait.
	 *
	 * @param remainingCursorItems How many baits should remain on the cursor after the application.
	 * @param fishingRod The fishing rod with the updated baits.
	 */
	public ApplicationResult(ItemStack fishingRod, int remainingCursorItems) {
		this.cursorItemModifier = remainingCursorItems;
		this.fishingRod = fishingRod;
	}

	/**
	 * @return How many baits should remain on the cursor after the application.
	 */
	public int getCursorItemModifier() {
		return cursorItemModifier;
	}

	/**
	 * @return The fishing rod with the updated baits.
	 */
	public ItemStack getFishingRod() {
		return fishingRod;
	}
}
