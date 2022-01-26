package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.utils.ItemFactory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Bait {

	private final ItemFactory itemFactory;

	List<Fish> fishList = new ArrayList<>();

	double boostRate;

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

	/**
	 * This adds a single fish to the list of fish this bait will affect. If adding a whole rarity, use the addRarity()
	 * method instead.
	 *
	 * @param f The fish being boosted.
	 */
	public void addFish(Fish f) {
		fishList.add(f);
	}

	/**
	 * This adds all the fish within a rarity to the boosted fish that this bait will affect.
	 *
	 * @param r The rarity having its fish added.
	 */
	public void addRarity(Rarity r) {
		fishList.addAll(EvenMoreFish.fishCollection.get(r));
	}

	/**
	 * @return The x multiplier of a chance to get one of the fish in the bait's fish to appear.
	 */
	public double getBoostRate() {
		return boostRate;
	}

	/**
	 * @param boostRate A percentage multiplier for how likely it is that a fish from this bait is fetched from random.
	 */
	public void setBoostRate(double boostRate) {
		this.boostRate = boostRate;
	}

	/**
	 * @return The list of fish this bait will boost the chances of catching.
	 */
	public List<Fish> getFishList() {
		return this.fishList;
	}
}
