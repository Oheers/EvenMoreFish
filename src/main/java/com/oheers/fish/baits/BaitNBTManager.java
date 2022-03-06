package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class BaitNBTManager {

	private static final NamespacedKey baitNBT = new NamespacedKey(JavaPlugin.getProvidingPlugin(BaitNBTManager.class), "emf-bait");
	private static final NamespacedKey baitedRodNBT = new NamespacedKey(JavaPlugin.getProvidingPlugin(BaitNBTManager.class), "emf-applied-bait");

	/**
	 * Checks whether the item has nbt to suggest it is a bait object.
	 *
	 * @param itemStack The item stack that could potentially be a bait.
	 * @return If the item stack is a bait or not (or if itemStack is null)
	 */
	public static boolean isBaitObject(ItemStack itemStack) {
		if (itemStack == null) return false;

		if (itemStack.hasItemMeta()) {
			return itemStack.getItemMeta().getPersistentDataContainer().has(baitNBT, PersistentDataType.STRING);
		} else return false;
	}

	/**
	 * @param itemStack The item stack that is a bait.
	 * @return The name of the bait.
	 */
	public static String getBaitName(ItemStack itemStack) {
		if (itemStack == null) return null;

		if (itemStack.hasItemMeta()) {
			return itemStack.getItemMeta().getPersistentDataContainer().get(baitNBT, PersistentDataType.STRING);
		} else return null;
	}

	/**
	 * Gives an ItemStack the nbt required for the plugin to see it as a valid bait that can be applied to fishing rods.
	 * It is inadvisable to use a block as a bait, as these will lose their nbt tags if they're placed - and the plugin
	 * will forget that it was ever a bait.
	 *
	 * @param item The item stack being turned into a bait.
	 * @param bait The name of the bait to be applied.
	 */
	public static void applyBaitNBT(ItemStack item, String bait) {
		if (item == null) return;

		if (item.hasItemMeta()) {
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.getPersistentDataContainer().set(baitNBT, PersistentDataType.STRING, bait);

			item.setItemMeta(itemMeta);
		}
	}

	/**
	 * This checks against the item's NBTs to work out whether the fishing rod passed through has applied baits.
	 *
	 * @param itemStack The fishing rod that could maybe have bait NBTs applied.
	 * @return Whether the fishing rod has bait NBT.
	 */
	public static boolean isBaitedRod(ItemStack itemStack) {
		if (itemStack == null) return false;
		if (itemStack.getType() != Material.FISHING_ROD) return false;

		if (itemStack.hasItemMeta()) {
			return itemStack.getItemMeta().getPersistentDataContainer().has(baitedRodNBT, PersistentDataType.STRING);
		}

		return false;
	}

	/**
	 * This applies a bait NBT reference to a fishing rod, and also checks whether the bait is already applied,
	 * making an effort to increase it rather than apply it.
	 *
	 * @param item The fishing rod having its bait applied.
	 * @param bait The name of the bait being applied.
	 * @param quantity The number of baits being applied. These must be of the same bait.
	 * @throws MaxBaitsReachedException When too many baits are tried to be applied to a fishing rod.
	 * @throws MaxBaitReachedException When one of the baits has hit maximum set by max-baits in baits.yml
	 * @returns An ApplicationResult containing the updated "item" itemstack and the remaining baits for the cursor.
	 */
	public static ApplicationResult applyBaitedRodNBT(ItemStack item, Bait bait, int quantity) throws MaxBaitsReachedException, MaxBaitReachedException {

		boolean doingLoreStuff = EvenMoreFish.baitFile.doRodLore();
		boolean maxBait = false;
		int cursorModifier = 0;

		if (isBaitedRod(item)) {

			try {
				if (doingLoreStuff) deleteOldLore(item);
			} catch (IndexOutOfBoundsException exception) {
				EvenMoreFish.logger.log(Level.SEVERE, "Failed to apply bait: " + bait + " to a user's fishing rod. This is likely caused by a change in format in the baits.yml config.");
				return null;
			}

			ItemMeta meta = item.getItemMeta();
			String[] baitList = meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING).split(",");
			StringBuilder combined = new StringBuilder();

			boolean foundBait = false;

			for (String s : baitList) {
				if (s.split(":")[0].equals(bait.getName())) {
					int newQuantity = Integer.parseInt(s.split(":")[1]) + quantity;

					if (newQuantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
						combined.append(s.split(":")[0]).append(":").append(bait.getMaxApplications()).append(",");
						// new cursor amt = -(max app - old app)
						cursorModifier = -bait.getMaxApplications() + (newQuantity - quantity);
						maxBait = true;
					} else if (newQuantity != 0) {
						combined.append(s.split(":")[0]).append(":").append(Integer.parseInt(s.split(":")[1]) + quantity).append(",");
						cursorModifier = -quantity;
					}
					foundBait = true;
				} else {
					combined.append(s).append(",");
				}
			}

			// We can manage the last character not being a colon if we have to add it in ourselves.
			if (!foundBait) {

				if (getNumBaitsApplied(item) >= EvenMoreFish.baitFile.getMaxBaits()) {
					// the lore's been taken out, we're not going to be doing anymore here, so we're just re-adding it now.
					if (doingLoreStuff) newApplyLore(item);
					throw new MaxBaitsReachedException("Max baits reached.");
				}

				if (quantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
					cursorModifier = -bait.getMaxApplications();
					maxBait = true;
				} else {
					combined.append(bait.getName()).append(":").append(quantity);
					cursorModifier = -quantity;
				}
			} else {
				if (combined.length() > 0) {
					combined.deleteCharAt(combined.length() - 1);
				}
			}

			if (combined.length() > 0) {
				meta.getPersistentDataContainer().set(baitedRodNBT, PersistentDataType.STRING, combined.toString());
			} else {
				meta.getPersistentDataContainer().remove(baitedRodNBT);
			}

			item.setItemMeta(meta);
		} else {
			ItemMeta meta = item.getItemMeta();
			if (quantity > bait.getMaxApplications() && bait.getMaxApplications() != -1) {
				meta.getPersistentDataContainer().set(baitedRodNBT, PersistentDataType.STRING, bait.getName() + ":" + bait.getMaxApplications());
				cursorModifier = -bait.getMaxApplications();
				maxBait = true;
			} else {
				meta.getPersistentDataContainer().set(baitedRodNBT, PersistentDataType.STRING, bait.getName() + ":" + quantity);
				cursorModifier = -quantity;
			}
			item.setItemMeta(meta);
		}

		if (doingLoreStuff) newApplyLore(item);

		if (maxBait) throw new MaxBaitReachedException(bait.getName() + " has reached its maximum number of uses on the fishing rod.");

		return new ApplicationResult(item, cursorModifier);
	}

	/**
	 * This fetches a random bait applied to the rod, based on the application-weight of the baits (if they exist). The
	 * weight defaults to "1" if there is no value applied for them.
	 *
	 * @param fishingRod The fishing rod.
	 * @return A random bait applied to the fishing rod.
	 */
	public static Bait randomBaitApplication(ItemStack fishingRod) {
		if (fishingRod.getItemMeta() == null) return null;

		ItemMeta meta = fishingRod.getItemMeta();
		String[] baitNameList = meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING).split(",");
		List<Bait> baitList = new ArrayList<>();

		for (String baitName : baitNameList) {

			Bait bait;
			if ((bait = EvenMoreFish.baits.get(baitName.split(":")[0])) != null) {
				baitList.add(bait);
			}

		}

		double totalWeight = 0;

		// Weighted random logic (nabbed from stackoverflow)
		for (Bait bait : baitList) {
			totalWeight += (bait.getApplicationWeight());
		}

		int idx = 0;
		for (double r = Math.random() * totalWeight; idx < baitList.size() - 1; ++idx) {
			r -= baitList.get(idx).getApplicationWeight();
			if (r <= 0.0) break;
		}

		return baitList.get(idx);
	}

	/**
	 * Calculates a random bait to throw out based on their catch-weight. It uses the same weight algorithm as
	 * randomBaitApplication, using the baits from the main class in the baits list.
	 *
	 * @return A random bait weighted by its catch-weight.
	 */
	public static Bait randomBaitCatch() {
		double totalWeight = 0;

		List<Bait> baitList = new ArrayList<>(EvenMoreFish.baits.values());

		// Weighted random logic (nabbed from stackoverflow)
		for (Bait bait : baitList) {
			totalWeight += (bait.getCatchWeight());
		}

		int idx = 0;
		for (double r = Math.random() * totalWeight; idx < EvenMoreFish.baits.size() - 1; ++idx) {
			r -= baitList.get(idx).getCatchWeight();
			if (r <= 0.0) break;
		}

		return baitList.get(idx);
	}

	/**
	 * Runs through the metadata of the rod to try and figure out whether a certain bait is applied or not.
	 *
	 * @param itemStack The fishing rod in item stack form.
	 * @param bait The name of the bait that could have been applied, must be the same as the time it was applied to the rod.
	 * @return If the fishing rod contains the bait or not.
	 */
	public static boolean hasBaitApplied(ItemStack itemStack, String bait) {
		ItemMeta meta = itemStack.getItemMeta();

		if (meta == null) return false;

		for (String appliedBait : meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING).split(",")) {
			if (appliedBait.split(":")[0].equals(bait)) return true;
		}

		return false;
	}

	public static void newApplyLore(ItemStack itemStack) {
		ItemMeta meta;
		if ((meta = itemStack.getItemMeta()) == null) return;

		List<String> lore;
		if ((lore = meta.getLore()) == null) lore = new ArrayList<>();

		for (String lineAddition : EvenMoreFish.baitFile.getRodLoreFormat()) {
			if (lineAddition.equals("{baits}")) {

				String rodNBT = meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING);
				if (rodNBT == null) return;

				int baitCount = 0;

				for (String bait : rodNBT.split(",")) {
					baitCount++;
					lore.add(new Message()
							.setMSG(EvenMoreFish.baitFile.getBaitFormat())
							.setAmount(bait.split(":")[1])
							.setBait(getBaitFormatted(bait.split(":")[0]))
							.toString());
				}

				if (EvenMoreFish.baitFile.showUnusedBaitSlots()) {
					for (int i = baitCount; i < EvenMoreFish.baitFile.getMaxBaits(); i++) {
						lore.add(FishUtils.translateHexColorCodes(EvenMoreFish.baitFile.unusedBaitSlotFormat()));
					}
				}
			} else {
				lore.add(new Message()
						.setMSG(lineAddition)
						.setCurrBaits(Integer.toString(getNumBaitsApplied(itemStack)))
						.setMaxBaits(Integer.toString(EvenMoreFish.baitFile.getMaxBaits()))
						.toString());
			}
		}

		meta.setLore(lore);
		itemStack.setItemMeta(meta);
	}

	/**
	 * This deletes all the old lore inserted by the plugin to the baited fishing rod. If the config value for the lore
	 * format had lines added/removed this will break the old rods.
	 *
	 * @throws IndexOutOfBoundsException When the fishing rod doesn't have enough lines of lore to delete, this could be
	 * caused by a modification to the format in the baits.yml config.
	 * @param itemStack The item stack having the bait section of its lore removed.
	 */
	public static void deleteOldLore(ItemStack itemStack) throws IndexOutOfBoundsException {
		ItemMeta meta;
		if ((meta = itemStack.getItemMeta()) == null) return;

		List<String> lore;
		if ((lore = meta.getLore()) == null) return;

		if (EvenMoreFish.baitFile.showUnusedBaitSlots()) {
			// starting at 1, because at least one bait replacing {baits} is repeated.
			for (int i = 1; i < EvenMoreFish.baitFile.getMaxBaits() + EvenMoreFish.baitFile.getRodLoreFormat().size(); i++) {
				lore.remove(lore.size()-1);
			}
		} else {
			// starting at 1, because at least one bait replacing {baits} is repeated.
			for (int i = 1; i < getNumBaitsApplied(itemStack) + EvenMoreFish.baitFile.getRodLoreFormat().size(); i++) {
				lore.remove(lore.size()-1);
			}
		}


		meta.setLore(lore);
		itemStack.setItemMeta(meta);
	}

	/**
	 * Works out how many baits are applied to an object based on the nbt data.
	 *
	 * @param itemStack The fishing rod with baits applied
	 * @return How many baits have been applied to this fishing rod.
	 */
	private static int getNumBaitsApplied(ItemStack itemStack) {

		ItemMeta meta;
		if ((meta = itemStack.getItemMeta()) == null) return 0;

		String rodNBT = meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING);
		if (rodNBT == null) return 1;

		return rodNBT.split(",").length;
	}

	/**
	 * Checks the bait from baitID to see if it has a displayname and returns that if necessary - else it just returns
	 * the baitID itself.
	 *
	 * @param baitID The baitID the bait is registered under in baits.yml
	 * @return How the bait should look in the lore of the fishing rod, for example.
	 */
	private static String getBaitFormatted(String baitID) {
		Bait bait = EvenMoreFish.baits.get(baitID);
		if (Objects.equals(bait.getDisplayName(), "")) return baitID;
		else return FishUtils.translateHexColorCodes(bait.getDisplayName());
	}
}