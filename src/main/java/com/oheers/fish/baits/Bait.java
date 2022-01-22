package com.oheers.fish.baits;

import com.oheers.fish.utils.ItemFactory;
import org.bukkit.inventory.ItemStack;

public class Bait {

	private final ItemFactory itemFactory;

	/**
	 * This represents a bait, which can be used to boost the likelihood that a certain fish or fish rarity appears from
	 * the rod. All data is fetched from the config when the Bait object is created and then can be given out using
	 * the create() method. An error will be printed out into the console if the name of the bait cannot be found in the
	 * baits.yml file.
	 *
	 * The plugin recognises the bait item from the create() method using NBT data, which can be applied using the
	 * BaitNBTManager class, which handles all the NBT thingies.
	 *
	 * @param name The name of the bait to be referenced from the baits.yml file
	 */
	public Bait(String name) {
		this.itemFactory = new ItemFactory("baits." + name);

		this.itemFactory.setItemGlowCheck(true);
		this.itemFactory.setItemDisplayNameCheck(true);
		this.itemFactory.setItemModelDataCheck(true);
		this.itemFactory.setItemDamageCheck(true);
		this.itemFactory.setItemDyeCheck(true);
	}

	/**
	 * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
	 * effects.
	 *
	 * @return An item stack representing the bait object, with nbt.
	 */
	public ItemStack create() {
		System.out.println("creating");
		return itemFactory.createItem();
	}
}
