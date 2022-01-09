package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class ItemFactory {

	private final String configLocation;

	private ItemStack product;

	private boolean itemRandom,
			itemModelDataCheck, itemDamageCheck, itemDyeCheck, itemGlowCheck;

	public ItemFactory(String configLocation) {
		this.configLocation = configLocation;
		this.product = getType();
	}

	/**
	 * Returns the item created, this is literally everything: NBT data, material, lore blah blah blah.
	 *
	 * @return The completed ItemStack
	 * @throws NullPointerException The type has not been enabled, therefore the ItemStack was never set in the first place.
	 */
	public ItemStack createItem() {
		if (itemRandom) this.product = getType();

		if (itemModelDataCheck) applyModelData();
		if (itemDamageCheck) applyDamage();
		if (itemDyeCheck) applyDyeColour();
		if (itemGlowCheck) applyGlow();

		return product;
	}

	/**
	 * Works out which material/skull the item needs to be, and sets the random setting to decide whether to call this method
	 * each time the give() method is run.
	 *
	 * @return The ItemStack of the item, with only skull metadata set if it's a player head.
	 */
	public ItemStack getType() {

		ItemStack oneMaterial = checkRandomMaterial();
		if (oneMaterial != null) return oneMaterial;

		ItemStack oneHead64 = checkRandomHead64();
		if (oneHead64 != null) return oneHead64;

		ItemStack oneHeadUUID = checkRandomHeadUUID();
		if (oneHeadUUID != null) return oneHeadUUID;

		ItemStack material = checkMaterial();
		if (material != null) return material;

		ItemStack head64 = checkHead64();
		if (head64 != null) return head64;

		ItemStack headUUID = checkHeadUUID();
		if (headUUID != null) return headUUID;

		// The fish has no item type specified
		return new ItemStack(Material.COD);

	}

	/**
	 * Checks for a value in the item.head-uuid setting for the item.
	 *
	 * @return Null if the setting doesn't exist, the head in ItemStack form if it does.
	 */
	private ItemStack checkHeadUUID() {
		String uValue = EvenMoreFish.fishFile.getConfig().getString(configLocation + ".item.head-uuid");

		// The fish has item: uuid selected
		// note - only works for players who have joined the server previously
		if (uValue != null) {
			ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) skull.getItemMeta();

			if (meta != null) {
				meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uValue)));
			}

			skull.setItemMeta(meta);
			return skull;
		}

		return null;
	}

	/**
	 * Checks for a value in the item.head-64 setting for the item.
	 *
	 * @return Null if the setting doesn't exist, the head in ItemStack form if it does.
	 */
	private ItemStack checkHead64() {
		// The fish has item: 64 selected
		String bValue = EvenMoreFish.fishFile.getConfig().getString(configLocation + ".item.head-64");
		if (bValue != null) {
			return FishUtils.get(bValue);
		}

		return null;
	}

	/**
	 * Checks for a value in the item.material setting for the item.
	 *
	 * @return Null if the setting doesn't exist, the item in ItemStack form if it does.
	 */
	private ItemStack checkMaterial() {
		// The fish has item: material selected
		String mValue = EvenMoreFish.fishFile.getConfig().getString(configLocation + ".item.material");
		if (mValue != null) {

			Material m;
			if ((m = Material.getMaterial(mValue.toUpperCase())) == null) {
				EvenMoreFish.logger.log(Level.SEVERE, configLocation + " has an incorrect assigned material: " + mValue);
				m = Material.COD;
			}

			return new ItemStack(m);
		}

		return null;
	}

	/**
	 * This chooses a random value from the item.materials setting for the item, it sets the randomItem boolean to true
	 * for the next time this is called.
	 *
	 * @return Null if the setting doesn't exist, a random ItemStack representation from a list of materials if it does.
	 */
	private ItemStack checkRandomMaterial() {

		List<String> lValues = EvenMoreFish.fishFile.getConfig().getStringList(configLocation + ".item.materials");
		if (lValues.size() > 0) {

			Random rand = new Random();

			Material m = Material.getMaterial(lValues.get(rand.nextInt(lValues.size())).toUpperCase());
			itemRandom = true;

			if (m == null) {
				EvenMoreFish.logger.log(Level.SEVERE, configLocation + "'s has an incorrect material name in its materials list.");
				for (String material : lValues) {
					if (Material.getMaterial(material.toUpperCase()) != null) {
						return new ItemStack(Objects.requireNonNull(Material.getMaterial(material.toUpperCase())));
					}
				}

				return new ItemStack(Material.COD);
			} else {
				return new ItemStack(m);
			}
		}

		return null;
	}

	/**
	 * This chooses a random value from the item.multiple-head-64 setting for the item, it sets the randomItem boolean to
	 * true for the next time this is called. A Steve/Alex head is dropped if the head-64 format is wonky and nothing is
	 * sent to console.
	 *
	 * @return Null if the setting doesn't exist, a random ItemStack representation from a list of head-64 values if it does.
	 */
	private ItemStack checkRandomHead64() {
		List<String> mh64Values = EvenMoreFish.fishFile.getConfig().getStringList(configLocation + ".item.multiple-head-64");
		if (mh64Values.size() > 0) {

			Random rand = new Random();

			String base64 = mh64Values.get(rand.nextInt(mh64Values.size()));
			itemRandom = true;

			return FishUtils.get(base64);
		}

		return null;
	}

	/**
	 * This chooses a random value from the item.multiple-head-uuid setting for the item, it sets the randomItem boolean to
	 * true for the next time this is called.
	 *
	 * @return Null if the setting doesn't exist, a random ItemStack representation from a list of head-uuid values if it does.
	 */
	private ItemStack checkRandomHeadUUID() {
		List<String> mhuValues = EvenMoreFish.fishFile.getConfig().getStringList(configLocation + ".item.multiple-head-uuid");
		if (mhuValues.size() > 0) {

			Random rand = new Random();

			String uuid = mhuValues.get(rand.nextInt(mhuValues.size()));
			itemRandom = true;

			try {
				ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta meta = (SkullMeta) skull.getItemMeta();

				if (meta != null) {
					meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
				}

				skull.setItemMeta(meta);
				return skull;
			} catch (IllegalArgumentException illegalArgumentException) {
				EvenMoreFish.logger.log(Level.SEVERE, "Could not load uuid: " + uuid + " as a multiple-head-uuid option for the config location" + configLocation);
				return new ItemStack(Material.COD);
			}
		}

		return null;
	}

	/**
	 * Adds an unsafe enchant of "Unbreaking I" to the object: the item flag to remove enchantments is not automatically
	 * added and needs to be the last part of the metadata added (if I'm correct in what I think).
	 * This requires that the item has been set using the setType() method.
	 */
	private void applyGlow() {
		if (EvenMoreFish.fishFile.getConfig().getBoolean(configLocation + ".glowing", false)) {
			this.product.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}
	}

	/**
	 * Takes a hex colour input from the dye-colour setting on the item and applies it to the object if it's an item that
	 * can be dyed (leather armour). The HIDE_DYE item flag is finally applied to the product's metadata.
	 */
	public void applyDyeColour() {
		String dyeColour = EvenMoreFish.fishFile.getConfig().getString(configLocation + ".dye-colour");

		if (dyeColour != null) {
			try {
				LeatherArmorMeta meta = (LeatherArmorMeta) product.getItemMeta();

				Color colour = Color.decode(dyeColour);

				if (meta != null) {
					meta.setColor(org.bukkit.Color.fromRGB(colour.getRed(), colour.getGreen(), colour.getBlue()));
					meta.addItemFlags(ItemFlag.HIDE_DYE);
				}

				product.setItemMeta(meta);
			} catch (ClassCastException exception) {
				EvenMoreFish.logger.log(Level.SEVERE, "Could not fetch hex value: " + dyeColour + " from config location + " + configLocation + ". Item is likely not a leather material.");
			}
		}
	}

	/**
	 * Applies a set amount of damage if the "durability" setting exists, and will apply a random amount of damage to the
	 * item if the config has random durability enabled.
	 */
	public void applyDamage() {

		ItemMeta meta = product.getItemMeta();
		if (meta instanceof Damageable) {
			Damageable nonDamaged = (Damageable) meta;

			int predefinedDamage = EvenMoreFish.fishFile.getConfig().getInt(configLocation + ".durability");
			if (predefinedDamage >= 0 && predefinedDamage <= 100) {
				nonDamaged.setDamage((int) ((100-predefinedDamage)/100.0 * product.getType().getMaxDurability()));
			} else {
				if (EvenMoreFish.mainConfig.doingRandomDurability()) {
					int max = product.getType().getMaxDurability();
					nonDamaged.setDamage((int) (Math.random() * (max + 1)));
				}
			}

			product.setItemMeta(nonDamaged);
		}
	}

	/**
	 * Adds model data to the item for use in custom texture packs etc.,
	 */
	private void applyModelData() {
		int value = EvenMoreFish.fishFile.getConfig().getInt(configLocation + ".item.custom-model-data");
		if (value != 0) {
			ItemMeta meta = product.getItemMeta();

			if (meta != null) {
				meta.setCustomModelData(value);
			}

			product.setItemMeta(meta);
		}
	}

	public void setItemModelDataCheck(boolean itemModelDataCheck) {
		this.itemModelDataCheck = itemModelDataCheck;
	}

	public void setItemDamageCheck(boolean itemDamageCheck) {
		this.itemDamageCheck = itemDamageCheck;
	}

	public void setItemDyeCheck(boolean itemDyeCheck) {
		this.itemDyeCheck = itemDyeCheck;
	}

	public void setItemGlowCheck(boolean itemGlowCheck) {
		this.itemGlowCheck = itemGlowCheck;
	}

	public Material getMaterial() {
		return product.getType();
	}
}