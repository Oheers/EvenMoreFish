package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.utils.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Bait {

	private final ItemFactory itemFactory;

	List<Fish> fishList = new ArrayList<>();
	List<Rarity> rarityList = new ArrayList<>();

	private final String name;
	private final String theme;

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
		this.name = name;

		if (EvenMoreFish.baitFile.getBaitTheme(name) != null) {
			this.theme = FishUtils.translateHexColorCodes(EvenMoreFish.baitFile.getBaitTheme(name));
		} else {
			this.theme = "&e";
		}

		this.itemFactory = new ItemFactory("baits." + name);

		this.itemFactory.setItemGlowCheck(true);
		this.itemFactory.setItemDisplayNameCheck(true);
		this.itemFactory.setItemModelDataCheck(true);
		this.itemFactory.setItemDamageCheck(true);
		this.itemFactory.setItemDyeCheck(true);

		this.itemFactory.setDisplayName(FishUtils.translateHexColorCodes("&e" + name));
	}

	/**
	 * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
	 * effects.
	 *
	 * @return An item stack representing the bait object, with nbt.
	 */
	public ItemStack create() {
		ItemStack baitItem = itemFactory.createItem();

		ItemMeta meta = baitItem.getItemMeta();
		if (meta != null) meta.setLore(createBoostLore());
		baitItem.setItemMeta(meta);

		BaitNBTManager.applyBaitNBT(baitItem, this.name);
		return baitItem;
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
		rarityList.add(r);
	}


	/**
	 * This fetches the boost's lore from the config and inserts the boost-rates into the {boosts} variable. This needs
	 * to be called after the bait theme is set and the boosts have been initialized, since it uses those variables.
	 */
	private List<String> createBoostLore() {

		List<String> lore = new ArrayList<>();

		for (String lineAddition : EvenMoreFish.baitFile.getBaitLoreFormat()) {
			if (lineAddition.equals("{boosts}")) {

				if (rarityList.size() > 0) {
					if (rarityList.size() > 1) lore.add(new Message().setMSG(EvenMoreFish.baitFile.getBoostRaritiesFormat()).setAmount(Integer.toString(rarityList.size())).setBaitTheme(theme).toString());
					else lore.add(new Message().setMSG(EvenMoreFish.baitFile.getBoostRarityFormat()).setAmount(Integer.toString(1)).setBaitTheme(theme).toString());
				}

				if (fishList.size() > 0) {
					lore.add(new Message().setMSG(EvenMoreFish.baitFile.getBoostFishFormat()).setAmount(Integer.toString(fishList.size())).setBaitTheme(theme).toString());
				}

			} else {
				lore.add(new Message()
						.setMSG(lineAddition)
						.setBaitTheme(theme)
						.toString());
			}
		}

		return lore;
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
