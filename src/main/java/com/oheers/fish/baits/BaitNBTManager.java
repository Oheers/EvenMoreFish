package com.oheers.fish.baits;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

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
		if (itemStack.getType() == Material.FISHING_ROD) return false;

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
	 */
	public static void applyBaitedRodNBT(ItemStack item, String bait) {
		if (isBaitedRod(item)) {

			ItemMeta meta = item.getItemMeta();
			String[] baitList = meta.getPersistentDataContainer().get(baitedRodNBT, PersistentDataType.STRING).split(":");
			StringBuilder combined = new StringBuilder();

			for (String s : baitList) {
				if (s.split(",")[0].equals(bait)) {
					s = s.split(",")[0] + "," + Integer.parseInt(s.split(",")[1]) + 1;
				}
				combined.append(s).append(":");
			}

			combined.deleteCharAt(combined.length()-1);

			meta.getPersistentDataContainer().set(baitedRodNBT, PersistentDataType.STRING, combined.toString());
		}
	}
}

























