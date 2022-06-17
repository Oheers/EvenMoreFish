package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            itemModelDataCheck, itemDamageCheck, itemDisplayNameCheck, itemDyeCheck, itemGlowCheck, itemPotionMetaCheck;

    private String displayName;

    private final FileConfiguration configurationFile;

    public ItemFactory(String configLocation) {
        this.configLocation = configLocation;
        this.configurationFile = getConfiguration();
        this.product = getType(null);
    }

    /**
     * Returns the item created, this is literally everything: NBT data, material, lore blah blah blah.
     *
     * @return The completed ItemStack
     * @throws NullPointerException The type has not been enabled, therefore the ItemStack was never set in the first place.
     */
    public ItemStack createItem(OfflinePlayer player) {
        if (itemRandom) this.product = getType(player);

        if (itemModelDataCheck) applyModelData();
        if (itemDamageCheck) applyDamage();
        if (itemDisplayNameCheck) applyDisplayName();
        if (itemDyeCheck) applyDyeColour();
        if (itemGlowCheck) applyGlow();
        if (itemPotionMetaCheck) applyPotionMeta();

        applyFlags();

        return product;
    }

    /**
     * Works out which material/skull the item needs to be, and sets the random setting to decide whether to call this method
     * each time the give() method is run.
     *
     * @return The ItemStack of the item, with only skull metadata set if it's a player head.
     */
    public ItemStack getType(OfflinePlayer player) {

        ItemStack oneMaterial = checkRandomMaterial();
        if (oneMaterial != null) return oneMaterial;

        ItemStack oneHead64 = checkRandomHead64();
        if (oneHead64 != null) return oneHead64;

        ItemStack oneHeadUUID = checkRandomHeadUUID();
        if (oneHeadUUID != null) return oneHeadUUID;

        ItemStack oneOwnHead = checkOwnHead(player);
        if (oneOwnHead != null) return oneOwnHead;

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
        String uValue = this.configurationFile.getString(configLocation + ".item.head-uuid");

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
        String bValue = this.configurationFile.getString(configLocation + ".item.head-64");
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
        String mValue = this.configurationFile.getString(configLocation + ".item.material");
        if (mValue == null) {
            return null;
        }

        //todo IA
        if (mValue.contains("itemsadder:")) {

            String[] splitMaterialValue = mValue.split(":");
            if (splitMaterialValue.length != 3) {
                EvenMoreFish.logger.severe(() -> String.format("%s has an incorrect assigned material: %s", configLocation, mValue));
                return new ItemStack(Material.COD);
            }

            final String namespaceId = splitMaterialValue[1] + ":" + splitMaterialValue[2];
            final CustomStack customStack = CustomStack.getInstance(namespaceId);
            if(customStack == null) {
                EvenMoreFish.logger.info(() -> String.format("Could not obtain itemsadder item %s",namespaceId));
                return new ItemStack(Material.COD);
            }
            return CustomStack.getInstance(namespaceId).getItemStack();
        }

        Material material = Material.getMaterial(mValue.toUpperCase());
        if (material == null) {
            EvenMoreFish.logger.severe(() -> String.format("%s has an incorrect assigned material: %s", configLocation, mValue));
            material = Material.COD;
        }

        return new ItemStack(material);

    }

    /**
     * This chooses a random value from the item.materials setting for the item, it sets the randomItem boolean to true
     * for the next time this is called.
     *
     * @return Null if the setting doesn't exist, a random ItemStack representation from a list of materials if it does.
     */
    private ItemStack checkRandomMaterial() {

        List<String> lValues = this.configurationFile.getStringList(configLocation + ".item.materials");
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
        List<String> mh64Values = this.configurationFile.getStringList(configLocation + ".item.multiple-head-64");
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
        List<String> mhuValues = this.configurationFile.getStringList(configLocation + ".item.multiple-head-uuid");
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
     * Each time this is run, the fisher's head is loaded as the skull and set into meta. This must be done each time otherwise
     * it just creates a null head. By default, when the plugin first loads it's given a null player so it'll need to given
     * a head before any giving is done.
     *
     * @returns A skull with the player's head.
     */
    private ItemStack checkOwnHead(OfflinePlayer player) {
        boolean ownHead = this.configurationFile.getBoolean(configLocation + ".item.own-head");
        // Causes this to run each turn the create() is called.
        itemRandom = ownHead;

        if (ownHead && player != null) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            if (meta != null) {
                meta.setOwningPlayer(player);
            } else {
                return new ItemStack(Material.COD);
            }

            skull.setItemMeta(meta);
            return skull;
        } else return null;
    }

    /**
     * Adds an unsafe enchant of "Unbreaking I" to the object: the item flag to remove enchantments is not automatically
     * added and needs to be the last part of the metadata added (if I'm correct in what I think).
     * This requires that the item has been set using the setType() method.
     */
    private void applyGlow() {
        if (this.configurationFile.getBoolean(configLocation + ".glowing", false)) {
            this.product.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
    }

    /**
     * Takes a hex colour input from the dye-colour setting on the item and applies it to the object if it's an item that
     * can be dyed (leather armour). The HIDE_DYE item flag is finally applied to the product's metadata.
     */
    public void applyDyeColour() {
        String dyeColour = this.configurationFile.getString(configLocation + ".dye-colour");

        if (dyeColour != null) {
            try {
                LeatherArmorMeta meta = (LeatherArmorMeta) product.getItemMeta();

                Color colour = Color.decode(dyeColour);

                if (meta != null) {
                    meta.setColor(org.bukkit.Color.fromRGB(colour.getRed(), colour.getGreen(), colour.getBlue()));
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

            int predefinedDamage = this.configurationFile.getInt(configLocation + ".durability");
            if (predefinedDamage >= 0 && predefinedDamage <= 100) {
                nonDamaged.setDamage((int) ((100 - predefinedDamage) / 100.0 * product.getType().getMaxDurability()));
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
        int value = this.configurationFile.getInt(configLocation + ".item.custom-model-data");
        if (value != 0) {
            ItemMeta meta = product.getItemMeta();

            if (meta != null) {
                meta.setCustomModelData(value);
            }

            product.setItemMeta(meta);
        }
    }

    /**
     * Applies a custom display name to the item, this is if server owners don't like the default colour or whatever their
     * reason is.
     */
    private void applyDisplayName() {
        String displayName = this.configurationFile.getString(configLocation + ".item.displayname");

        if (displayName == null && this.displayName != null) displayName = this.displayName;

        if (displayName != null) {
            ItemMeta meta = product.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(FishUtils.translateHexColorCodes(displayName));
            }

            product.setItemMeta(meta);
        }
    }

    /**
     * The fish is a material of type POTION, if this isn't done it's just an "Uncraftable Potion" which obviously isn't
     * desired. If the material isn't a potion then it won't be added and a message will be thrown to the console to
     * alert the admins.
     */
    private void applyPotionMeta() {
        String potionSettings = this.configurationFile.getString(configLocation + ".item.potion");

        if (potionSettings == null) return;
        if (!(product.getItemMeta() instanceof PotionMeta)) return;

        String[] split = potionSettings.split(":");
        if (split.length != 3) {
            EvenMoreFish.logger.log(Level.SEVERE, configLocation + ".item.potion: is formatted incorrectly in the fish.yml file. Use \"potion:duration:amplifier\".");
        }

        PotionMeta meta = ((PotionMeta) product.getItemMeta());
        try {
            meta.addCustomEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(split[0])), Integer.parseInt(split[1]) * 20, Integer.parseInt(split[2]) - 1, false), true);
        } catch (NumberFormatException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, configLocation + ".item.potion: is formatted incorrectly in the fish.yml file. Use \"potion:duration:amplifier\", where duration & amplifier are integer values.");
        } catch (NullPointerException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, configLocation + ".item.potion: " + split[0] + " is not a valid potion name. A list can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
        }

        product.setItemMeta(meta);

    }

    /**
     * Applies flags that hide unnecessary information from players such as enchantments (glow effect), dye colour and
     * future stuff. This is always run whenever an item is created and checks whether it's even worth adding some flags
     * before they're added.
     */
    private void applyFlags() {
        ItemMeta meta = product.getItemMeta();

        if (meta != null) {
            if (itemDyeCheck) meta.addItemFlags(ItemFlag.HIDE_DYE);
            if (itemGlowCheck) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            this.product.setItemMeta(meta);
        }
    }

    private FileConfiguration getConfiguration() {
        if (this.configLocation.startsWith("fish.")) {
            return EvenMoreFish.fishFile.getConfig();
        } else if (this.configLocation.startsWith("baits.")) {
            return EvenMoreFish.baitFile.getConfig();
        } else {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not fetch file configuration for: " + this.configLocation);
            return null;
        }
    }

    /**
     * This is used if a displayname can't be found for the item, however it requires that the displayname check is being
     * done on the item.
     *
     * @param displayName The displayname for the item stack.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public void setItemDisplayNameCheck(boolean itemDisplayNameCheck) {
        this.itemDisplayNameCheck = itemDisplayNameCheck;
    }

    public void setPotionMetaCheck(boolean metaPotionCheck) {
        this.itemPotionMetaCheck = metaPotionCheck;
    }

    public Material getMaterial() {
        return product.getType();
    }
}