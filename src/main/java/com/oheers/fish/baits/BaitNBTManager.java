package com.oheers.fish.baits;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class BaitNBTManager {

	private static final NamespacedKey baitNBT = new NamespacedKey(JavaPlugin.getProvidingPlugin(BaitNBTManager.class), "emf-bait");

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
}
