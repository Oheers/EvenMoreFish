package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * @returns The item parameter with baited NBT added to it.
	 */
	public static ItemStack applyBaitedRodNBT(ItemStack item, String bait) {

		applyLore(item, bait);

		if (isBaitedRod(item)) {

			ItemMeta meta = item.getItemMeta();
			String[] baitList = meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING).split(",");
			StringBuilder combined = new StringBuilder();

			boolean foundBait = false;

			for (String s : baitList) {
				if (s.split(":")[0].equals(bait)) {
					combined.append(s.split(":")[0]).append(":").append(Integer.parseInt(s.split(":")[1]) + 1).append(",");
					foundBait = true;
				} else {
					combined.append(s).append(",");
				}
			}

			// We can manage the last character not being a colon if we have to add it in ourselves.
			if (!foundBait) {
				combined.append(bait).append(":").append(1);
			} else {
				combined.deleteCharAt(combined.length() - 1);
			}

			meta.getPersistentDataContainer().set(baitedRodNBT, PersistentDataType.STRING, combined.toString());
			item.setItemMeta(meta);
		} else {
			ItemMeta meta = item.getItemMeta();
			meta.getPersistentDataContainer().set(baitedRodNBT, PersistentDataType.STRING, bait + ":1");
			item.setItemMeta(meta);
		}

		return item;
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

	/**
	 * This calculates how the new lore should look for the fishing rod. It uses the config options in baits.yml to
	 * decide how the formatting for the new rod's lore should look, increasing any numbers that need increasing by
	 * finding the respective line for the bait, or adding a new one in.
	 *
	 * @param itemStack The fishing rod in item stack form.
	 * @param bait The name identifier for the bait.
	 */
	public static void applyLore(ItemStack itemStack, String bait) {

		ItemMeta meta;
		if ((meta = itemStack.getItemMeta()) == null) return;

		List<String> lore = meta.getLore();

		boolean isBaited;

		LORE_APPLICATION: {
			if ((isBaited = isBaitedRod(itemStack)) && hasBaitApplied(itemStack, bait)) {

				if (lore == null) lore = new ArrayList<>();

				Map.Entry<Integer, Integer> editInfo;
				if ((editInfo = findBaitLine(itemStack, bait)).getKey() == -1) return;

				lore.set(editInfo.getKey(), new Message().setMSG(EvenMoreFish.baitFile.getBaitFormat()).setAmount(Integer.toString(editInfo.getValue())).setBait(bait).toString());
			} else {

				if (lore == null) lore = new ArrayList<>();

				if (isBaited) {
					lore.add(lore.size() - getGapAfterBaits(), new Message().setMSG(EvenMoreFish.baitFile.getBaitFormat()).setAmount("1").setBait(bait).toString());
					break LORE_APPLICATION;
				}

				for (String lineAddition : EvenMoreFish.baitFile.getLoreFormat()) {
					if (lineAddition.equals("{baits}")) {
						lore.add(new Message().setMSG(EvenMoreFish.baitFile.getBaitFormat()).setAmount("1").setBait(bait).toString());
					} else {
						lore.add(FishUtils.translateHexColorCodes(lineAddition));
					}
				}
			}
		}

		meta.setLore(lore);
		itemStack.setItemMeta(meta);

	}

	/**
	 * This finds the line of the lore that the fishing rod has that indicates to the bait mentioned, so it can be modified
	 * to increase/decrease the value.
	 *
	 * @param itemStack The fishing rod
	 * @return The line (0 index) that the bait is in as the key and the amount of baits + 1 applied as the value.
	 */
	private static Map.Entry<Integer, Integer> findBaitLine(ItemStack itemStack, String bait) {

		int afterCounter;
		if (itemStack.getItemMeta() != null) {
			afterCounter = (itemStack.getItemMeta().getLore().size() - 1) - getGapAfterBaits();
		} else {
			afterCounter = getGapAfterBaits();
		}

		// Prevents the plugin messing up the lore in the case of a problem reading the lore format.
		if (afterCounter == -1) return new AbstractMap.SimpleEntry<>(-1, -1);

		// Puts the afterCounter to the start of the baits in the lore
		String[] appliedBaits = itemStack.getItemMeta().getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING).split(",");
		afterCounter -= (appliedBaits.length - 1);

		for (String baitStore : appliedBaits) {
			if (baitStore.split(":")[0].equals(bait)) {
				return new AbstractMap.SimpleEntry<>(afterCounter, Integer.parseInt(baitStore.split(":")[1]) + 1);
			} else {
				afterCounter++;
			}
		}

		// Something failed, the rod doesn't have the bait parameter applied.
		return new AbstractMap.SimpleEntry<>(-1, -1);
	}

	/**
	 * @return How many lines are after {baits} in the baits.yml format.lore section.
	 */
	private static int getGapAfterBaits() {
		// How many lines after the baits are added.
		int afterCounter = 0;

		boolean failedCheck = true;

		List<String> loreFormat = EvenMoreFish.baitFile.getLoreFormat();
		for (int i = loreFormat.size() - 1; i >= 0; i--) {
			if (loreFormat.get(i).equals("{baits}")) {
				failedCheck = false;
				break;
			} else {
				afterCounter++;
			}
		}

		if (failedCheck) return -1;
		else return afterCounter;
	}
}