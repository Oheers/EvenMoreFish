package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.OldMessage;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.utils.ItemFactory;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bait {

	private final ItemFactory itemFactory;

	List<Fish> fishList = new ArrayList<>();
	List<Rarity> rarityList = new ArrayList<>();

	Set<Rarity> fishListRarities = new HashSet<>();

	private final String name, displayName, theme;

	private final int maxApplications, dropQuantity;

	double boostRate, applicationWeight, catchWeight;

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

		setApplicationWeight(EvenMoreFish.baitFile.getApplicationWeight(name));
		setCatchWeight(EvenMoreFish.baitFile.getCatchWeight(name));

		this.boostRate = EvenMoreFish.baitFile.getBoostRate();
		this.maxApplications = EvenMoreFish.baitFile.getMaxBaitApplication(this.name);
		this.displayName = EvenMoreFish.baitFile.getDisplayName(this.name);
		this.dropQuantity = EvenMoreFish.baitFile.getDropQuantity(this.name);

		this.itemFactory = new ItemFactory("baits." + name);

		this.itemFactory.setItemGlowCheck(true);
		this.itemFactory.setItemDisplayNameCheck(true);
		this.itemFactory.setItemModelDataCheck(true);
		this.itemFactory.setItemDamageCheck(true);
		this.itemFactory.setItemDyeCheck(true);
		this.itemFactory.setPotionMetaCheck(true);

		this.itemFactory.setDisplayName(FishUtils.translateHexColorCodes("&e" + name));
	}

	/**
	 * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
	 * effects.
	 *
	 * @return An item stack representing the bait object, with nbt.
	 */
	public ItemStack create(OfflinePlayer player) {
		ItemStack baitItem = itemFactory.createItem(player);
		baitItem.setAmount(dropQuantity);

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
		fishListRarities.add(f.getRarity());
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
					if (rarityList.size() > 1) lore.add(new OldMessage().setMSG(EvenMoreFish.baitFile.getBoostRaritiesFormat()).setAmount(Integer.toString(rarityList.size())).setBaitTheme(theme).toString());
					else lore.add(new OldMessage().setMSG(EvenMoreFish.baitFile.getBoostRarityFormat()).setAmount(Integer.toString(1)).setBaitTheme(theme).toString());
				}

				if (fishList.size() > 0) {
					lore.add(new OldMessage().setMSG(EvenMoreFish.baitFile.getBoostFishFormat()).setAmount(Integer.toString(fishList.size())).setBaitTheme(theme).toString());
				}

			} else if (lineAddition.equals("{lore}")) {
				EvenMoreFish.baitFile.getLore(this.name).stream().forEach(line -> {
					lore.add(new OldMessage()
						.setMSG(line)
						.toString());
				});
			} else {
				lore.add(new OldMessage()
						.setMSG(lineAddition)
						.setBaitTheme(theme)
						.toString());
			}
		}

		return lore;
	}

	/**
	 * This chooses a random fish based on the set boosts of the bait's config.
	 *
	 * If there's rarities in the rarityList, choose a rarity first, applying multiplication of weight.
	 * If there's no rarities in the server list: *
	 * Check if there's any fish in the bait for this rarity, boost them. REMOVE BAIT
	 * If the rarity chosen was not boosted, check if any fish are in this rarity and boost them. REMOVE BAIT
	 *
	 * * Pick a rarity, boosting all rarities referenced in the fishList, from that rarity choose a random fish, if that
	 * fish is within the fishList then give it to the player as the fish roll. REMOVE BAIT
	 * @return A chosen fish.
	 */
	public Fish chooseFish(Player player, Location location) {
		Set<Rarity> boostedRarities = new HashSet<>(getRarityList());
		boostedRarities.addAll(fishListRarities);

		Rarity fishRarity = FishingProcessor.randomWeightedRarity(player, getBoostRate(), boostedRarities, EvenMoreFish.fishCollection.keySet());
		Fish fish;

		if (getFishList().size() > 0) {
			// The bait has both rarities: and fish: set but the plugin chose a rarity with no boosted fish. This ensures
			// the method isn't given an empty list.
			if (!fishListRarities.contains(fishRarity)) {
				fish = FishingProcessor.getFish(fishRarity, location, player, EvenMoreFish.baitFile.getBoostRate(), EvenMoreFish.fishCollection.get(fishRarity));
			} else {
				fish = FishingProcessor.getFish(fishRarity, location, player, EvenMoreFish.baitFile.getBoostRate(), getFishList());
			}

			if (!getRarityList().contains(fishRarity) && (fish == null || !getFishList().contains(fish))) {
				// boost effect chose a fish but the randomizer didn't pick out the right fish - they've been incorrectly boosted.
				return FishingProcessor.getFish(fishRarity, location, player, 1, null);
			} else {
				alertUsage(player);
			}
		} else {
			fish = FishingProcessor.getFish(fishRarity, location, player, 1, null);
			if (getRarityList().contains(fishRarity)) {
				alertUsage(player);
			}
		}

		return fish;
	}

	/**
	 * Lets the player know that they've used one of their baits. Uses the value in messages.yml under "bait-use".
	 *
	 * @param player The player that's used the bait.
	 */
	private void alertUsage(Player player) {
		Message message = new Message(ConfigMessage.BAIT_USED);
		message.setBait(this.name);
		message.setBaitTheme(this.theme);
		message.broadcast(player, true, true);
	}

	/**
	 * @return How likely the bait is to apply out of all others applied baits.
	 */
	public double getApplicationWeight() {
		return applicationWeight;
	}

	/**
	 * @param applicationWeight How likely the bait is to apply out of all others applied baits.
	 */
	public void setApplicationWeight(double applicationWeight) {
		this.applicationWeight = applicationWeight;
	}

	/**
	 * @return How likely the bait is to appear out of all other baits when caught.
	 */
	public double getCatchWeight() {
		return catchWeight;
	}

	/**
	 * @param catchWeight How likely the bait is to appear out of all other baits when caught.
	 */
	public void setCatchWeight(double catchWeight) {
		this.catchWeight = catchWeight;
	}

	/**
	 * @return The x multiplier of a chance to get one of the fish in the bait's fish to appear.
	 */
	public double getBoostRate() {
		return boostRate;
	}

	/**
	 * @param boostRate The x multiplier of a chance to get one of the fish in the bait's fish to appear.
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

	/**
	 * @return The list of rarities this bait will boost the chances of catching.
	 */
	public List<Rarity> getRarityList() {
		return this.rarityList;
	}

	/**
	 * @return The name identifier of the bait.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The colour theme defined for the bait.
	 */
	public String getTheme() {
		return theme;
	}

	/**
	 * @return How many of this bait can be applied to a fishing rod.
	 */
	public int getMaxApplications() {
		return maxApplications;
	}

	/**
	 * @return The displayname setting for the bait.
	 */
	public String getDisplayName() {
		return displayName;
	}
}
