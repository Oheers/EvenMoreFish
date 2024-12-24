package com.oheers.fish.utils;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.api.addons.exceptions.IncorrectAssignedMaterialException;
import com.oheers.fish.api.addons.exceptions.NoPrefixException;
import com.oheers.fish.config.MainConfig;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ItemFactory {

    private final String configLocation;
    private final Section configurationFile;
    private ItemStack product;
    private int chosenRandomIndex = -1;
    private boolean itemRandom;
    private boolean rawMaterial;
    private boolean itemModelDataCheck;
    private boolean itemDamageCheck;
    private boolean itemDisplayNameCheck;
    private boolean itemDyeCheck;
    private boolean itemGlowCheck;
    private boolean itemLoreCheck;
    private boolean itemPotionMetaCheck;
    private String displayName;

    /**
     * Creates an instance of ItemFactory based on the provided Section
     * @param configLocation The location of the item config
     * @param configurationFile The config to check
     */
    public ItemFactory(@Nullable String configLocation, @NotNull Section configurationFile) {
        if (configLocation != null && !configLocation.isEmpty()) {
            this.configLocation = configLocation + ".";
        } else {
            this.configLocation = "";
        }
        this.configurationFile = configurationFile;
        this.rawMaterial = false;
        this.product = getType(null);
    }

    /**
     * Returns the item created, this is literally everything: NBT data, material, lore blah blah blah.
     *
     * @return The completed ItemStack
     * @throws NullPointerException The type has not been enabled, therefore the ItemStack was never set in the first place.
     */
    public ItemStack createItem(OfflinePlayer player, int randomIndex) {
        return createItem(player, randomIndex, null);
    }

    /**
     * Returns the item created, this is literally everything: NBT data, material, lore blah blah blah.
     * Optionally provide a map of variables to parse in the display and lore.
     *
     * @return The completed ItemStack
     * @throws NullPointerException The type has not been enabled, therefore the ItemStack was never set in the first place.
     */
    public ItemStack createItem(OfflinePlayer player, int randomIndex, @Nullable Map<String, String> replacements) {
        if (rawMaterial) return this.product;
        if (itemRandom && player != null) {
            if (randomIndex == -1) this.product = getType(player);
            else this.product = setType(randomIndex);
        }

        if (itemModelDataCheck) applyModelData();
        if (itemDamageCheck) applyDamage();
        if (itemDisplayNameCheck) applyDisplayName(replacements);
        if (itemDyeCheck) applyDyeColour();
        if (itemGlowCheck) applyGlow();
        if (itemLoreCheck) applyLore(replacements);
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

        ItemStack raw = checkRaw();
        if (raw != null) return raw;

        ItemStack material = checkMaterial();
        if (material != null) return material;

        ItemStack rawMaterial = checkRawMaterial();
        if (rawMaterial != null) return rawMaterial;

        ItemStack oneMaterial = checkRandomMaterial(-1);
        if (oneMaterial != null) return oneMaterial;

        ItemStack oneHeadDB = checkRandomHeadDB(-1);
        if (oneHeadDB != null) return oneHeadDB;

        ItemStack oneHead64 = checkRandomHead64(-1);
        if (oneHead64 != null) return oneHead64;

        ItemStack oneHeadUUID = checkRandomHeadUUID(-1);
        if (oneHeadUUID != null) return oneHeadUUID;

        ItemStack oneOwnHead = checkOwnHead(player);
        if (oneOwnHead != null) return oneOwnHead;

        ItemStack headDB = checkHeadDB();
        if (headDB != null) return headDB;

        ItemStack head64 = checkHead64();
        if (head64 != null) return head64;

        ItemStack headUUID = checkHeadUUID();
        if (headUUID != null) return headUUID;

        // The fish has no item type specified
        return new ItemStack(Material.COD);

    }

    /**
     * Sets the index for a random item based on its location in the config. For example, if four values are given and
     * randomIndex = 0, the first one is given.
     *
     * @param randomIndex The index to use.
     * @return The type for the fish based on the random index.
     */
    public ItemStack setType(int randomIndex) {
        ItemStack oneHeadDB = checkRandomHeadDB(randomIndex);
        if (oneHeadDB != null) return oneHeadDB;

        ItemStack oneMaterial = checkRandomMaterial(randomIndex);
        if (oneMaterial != null) return oneMaterial;

        ItemStack oneHead64 = checkRandomHead64(randomIndex);
        if (oneHead64 != null) return oneHead64;

        ItemStack oneHeadUUID = checkRandomHeadUUID(randomIndex);
        if (oneHeadUUID != null) return oneHeadUUID;

        return new ItemStack(Material.COD);
    }

    /**
     * Checks for a value in the item.head-uuid setting for the item.
     *
     * @return Null if the setting doesn't exist, the head in ItemStack form if it does.
     */
    private @Nullable ItemStack checkHeadUUID() {
        String uValue = this.configurationFile.getString(configLocation + "item.head-uuid");

        // The fish has item: uuid selected
        // note - only works for players who have joined the server previously
        if (uValue != null) {
            return FishUtils.getSkullFromUUID(UUID.fromString(uValue));
        }

        return null;
    }

    /**
     * Checks for a value in the item.head-64 setting for the item.
     *
     * @return Null if the setting doesn't exist, the head in ItemStack form if it does.
     */
    private @Nullable ItemStack checkHead64() {
        // The fish has item: 64 selected
        String bValue = this.configurationFile.getString(configLocation + "item.head-64");
        if (bValue != null) {
            return FishUtils.getSkullFromBase64(bValue);
        }

        return null;
    }

    /**
     * Checks for a value in the item.headdb setting for this item.
     *
     * @return Null if the setting doesn't exist, the head from HDB in ItemStack form if it does.
     */
    private @Nullable ItemStack checkHeadDB() {
        // The fish has item: headdb selected
        if (!EvenMoreFish.getInstance().isUsingHeadsDB()) return null;

        int headID = this.configurationFile.getInt(configLocation + "item.headdb", -1);
        if (headID != -1) {
            return EvenMoreFish.getInstance().getHDBapi().getItemHead(Integer.toString(headID));
        }

        return null;
    }

    /**
     * Checks for a value in the item.raw setting for the item.
     *
     * @return Null if the setting doesn't exist or is invalid, the item in ItemStack form if it does.
     */
    private ItemStack checkRaw() {
        // The fish has item.raw selected
        String rawValue = this.configurationFile.getString(configLocation + "item.raw-nbt");
        if (rawValue == null) {
            return null;
        }
        ItemStack item = null;
        try {
            item = NBT.itemStackFromNBT(NBT.parseNBT(rawValue));
        } catch (NbtApiException exception) {
            EvenMoreFish.getInstance().getLogger().severe(configLocation + " has invalid raw NBT: " + rawValue);
        }
        if (item == null) {
            return null;
        }
        rawMaterial = true;
        return item;
    }

    /**
     * Checks for a value in the item.material setting for the item.
     *
     * @return Null if the setting doesn't exist, the item in ItemStack form if it does.
     */
    private ItemStack checkMaterial() {
        // The fish has item: material selected
        String mValue = this.configurationFile.getString(configLocation + "item.material");
        if (mValue == null) {
            return null;
        }

        Material material = Material.getMaterial(mValue.toUpperCase());
        if (material == null) {
            ItemStack customItemStack = checkMaterial(mValue);
            if (customItemStack != null) { return customItemStack; }
            EvenMoreFish.getInstance().getLogger().severe(() -> String.format("%s has an incorrect assigned material: %s", configLocation, mValue));
            material = Material.COD;
        }

        return new ItemStack(material);
    }

    private ItemStack checkMaterial(String mValue) {
        if (mValue == null) {
            return null;
        }

        Material material = Material.getMaterial(mValue.toUpperCase());
        if (material == null) {
            ItemStack customItemStack = checkItem(mValue);
            if (customItemStack != null) {
                return customItemStack;
            }
            material = Material.COD;
        }

        return new ItemStack(material);
    }

    private ItemStack checkItem(final String materialId) {
        if (materialId == null) {
            return null;
        }

        rawMaterial = false;

        try {
            return getItem(materialId);
        } catch (NoPrefixException | IncorrectAssignedMaterialException e) {
            rawMaterial = true;
            EvenMoreFish.getInstance().getLogger().warning(e::getMessage);
            return new ItemStack(Material.COD);
        }
    }

    /**
     * This chooses a random value from the item.materials setting for the item, it sets the randomItem boolean to true
     * for the next time this is called.
     *
     * @return Null if the setting doesn't exist, a random ItemStack representation from a list of materials if it does.
     */
    private ItemStack checkRandomMaterial(int randomIndex) {

        List<String> lValues = this.configurationFile.getStringList(configLocation + "item.materials");
        if (!lValues.isEmpty()) {

            final Random rand = EvenMoreFish.getInstance().getRandom();

            if (randomIndex == -1 || randomIndex + 1 > lValues.size()) {
                randomIndex = rand.nextInt(lValues.size());
                this.chosenRandomIndex = randomIndex;
            }

            ItemStack customItemStack = checkMaterial(lValues.get(randomIndex));
            itemRandom = true;

            if (customItemStack == null) {
                EvenMoreFish.getInstance().getLogger().severe(configLocation + "'s has an incorrect material name in its materials list.");
                for (String material : lValues) {
                    ItemStack item = checkMaterial(material);
                    if (item != null) {
                        return item;
                    }
                }
                return new ItemStack(Material.COD);
            } else {
                return customItemStack;
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
    private ItemStack checkRandomHead64(int randomIndex) {
        List<String> mh64Values = this.configurationFile.getStringList(configLocation + "item.multiple-head-64");
        if (!mh64Values.isEmpty()) {

            final Random rand = EvenMoreFish.getInstance().getRandom();

            if (randomIndex == -1 || randomIndex + 1 > mh64Values.size()) {
                randomIndex = rand.nextInt(mh64Values.size());
            }

            this.chosenRandomIndex = randomIndex;

            String base64 = mh64Values.get(randomIndex);
            itemRandom = true;

            return FishUtils.getSkullFromBase64(base64);
        }

        return null;
    }

    private ItemStack checkRandomHeadDB(int randomIndex) {
        // The fish has item: headdb selected
        if (!EvenMoreFish.getInstance().isUsingHeadsDB()) return null;

        List<Integer> headIDs = this.configurationFile.getIntList(configLocation + "item.multiple-headdb");
        if (!headIDs.isEmpty()) {
            final Random rand = EvenMoreFish.getInstance().getRandom();

            if (randomIndex == -1 || randomIndex + 1 > headIDs.size()) {
                randomIndex = rand.nextInt(headIDs.size());
            }

            this.chosenRandomIndex = randomIndex;

            int headID = headIDs.get(randomIndex);
            itemRandom = true;

            return EvenMoreFish.getInstance().getHDBapi().getItemHead(Integer.toString(headID));
        }

        return null;
    }

    /**
     * This chooses a random value from the item.multiple-head-uuid setting for the item, it sets the randomItem boolean to
     * true for the next time this is called.
     *
     * @return Null if the setting doesn't exist, a random ItemStack representation from a list of head-uuid values if it does.
     */
    private @Nullable ItemStack checkRandomHeadUUID(int randomIndex) {
        List<String> mhuValues = this.configurationFile.getStringList(configLocation + "item.multiple-head-uuid");
        if (!mhuValues.isEmpty()) {

            final Random rand = EvenMoreFish.getInstance().getRandom();

            if (randomIndex == -1 || randomIndex + 1 > mhuValues.size()) {
                randomIndex = rand.nextInt(mhuValues.size());
                this.chosenRandomIndex = randomIndex;
            }

            String uuid = mhuValues.get(randomIndex);
            itemRandom = true;

            try {
                return FishUtils.getSkullFromUUID(UUID.fromString(uuid));
            } catch (IllegalArgumentException illegalArgumentException) {
                EvenMoreFish.getInstance().getLogger().severe("Could not load uuid: " + uuid + " as a multiple-head-uuid option for the config location" + configLocation);
                return new ItemStack(Material.COD);
            }
        }

        return null;
    }

    /**
     * Each time this is run, the fisher's head is loaded as the skull and set into meta. This must be done each time otherwise
     * it just creates a null head. By default, when the plugin first loads it's given a null player so it'll need to given
     * a head before any giving is done.
     * <p>
     * {@code @returns} A skull with the player's head.
     */
    private ItemStack checkOwnHead(OfflinePlayer player) {
        boolean ownHead = this.configurationFile.getBoolean(configLocation + "item.own-head");
        // Causes this to run each turn the create() is called.
        itemRandom = ownHead;

        if (ownHead && player != null) {
            return FishUtils.getSkullFromUUID(player.getUniqueId());
        } else return null;
    }

    /**
     * Checks for the raw-material type, returning the same as "material" but setting the rawMaterial boolean to true.
     * This causes the item to not be given NBT data.
     *
     * @return A raw itemstack with the material provided after setting rawMaterial to true.
     */
    private ItemStack checkRawMaterial() {
        String materialID = this.configurationFile.getString(configLocation + "item.raw-material");
        if(materialID != null) {
            rawMaterial = true;
        }

        return checkItem(materialID);
    }

    public ItemStack getItem(final @NotNull String materialString) throws IncorrectAssignedMaterialException, NoPrefixException {
        if (materialString.contains(":")) {
            //assume this is an addon string
            final String[] split = materialString.split(":", 2);
            final String prefix = split[0];
            final String id = split[1];
            return EvenMoreFish.getInstance().getAddonManager().getItemStack(prefix,id);
        }


        Material material = Material.matchMaterial(materialString);
        if (material == null) {
            throw new IncorrectAssignedMaterialException(configLocation, materialString);
        }

        return new ItemStack(material);
    }

    /**
     * Adds an unsafe enchant of "Unbreaking I" to the object: the item flag to remove enchantments is not automatically
     * added and needs to be the last part of the metadata added (if I'm correct in what I think).
     * This requires that the item has been set using the setType() method.
     */
    private void applyGlow() {
        if (this.configurationFile.getBoolean(configLocation + "glowing", false)) {
            this.product.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
    }

    /**
     * Takes a hex colour input from the dye-colour setting on the item and applies it to the object if it's an item that
     * can be dyed (leather armour). The HIDE_DYE item flag is finally applied to the product's metadata.
     */
    public void applyDyeColour() {
        String dyeColour = this.configurationFile.getString(configLocation + "dye-colour");

        if (dyeColour != null) {
            try {
                LeatherArmorMeta meta = (LeatherArmorMeta) product.getItemMeta();

                Color colour = Color.decode(dyeColour);

                if (meta != null) {
                    meta.setColor(org.bukkit.Color.fromRGB(colour.getRed(), colour.getGreen(), colour.getBlue()));
                }

                product.setItemMeta(meta);
            } catch (ClassCastException exception) {
                EvenMoreFish.getInstance().getLogger().severe("Could not fetch hex value: " + dyeColour + " from config location + " + configLocation + " Item is likely not a leather material.");
            }
        }
    }

    /**
     * Applies a set amount of damage if the "durability" setting exists, and will apply a random amount of damage to the
     * item if the config has random durability enabled.
     */
    public void applyDamage() {

        ItemMeta meta = product.getItemMeta();
        if (meta instanceof Damageable nonDamaged) {

            int predefinedDamage = this.configurationFile.getInt(configLocation + "durability");
            if (predefinedDamage >= 0 && predefinedDamage <= 100) {
                nonDamaged.setDamage((int) (predefinedDamage / 100.0 * product.getType().getMaxDurability()));
            } else {
                if (MainConfig.getInstance().doingRandomDurability()) {
                    int max = product.getType().getMaxDurability();
                    nonDamaged.setDamage(EvenMoreFish.getInstance().getRandom().nextInt() * (max + 1));
                }
            }

            product.setItemMeta(nonDamaged);
        }
    }

    /**
     * Adds model data to the item for use in custom texture packs etc.,
     */
    private void applyModelData() {
        int value = this.configurationFile.getInt(configLocation + "item.custom-model-data");
        if (value != 0) {
            ItemMeta meta = product.getItemMeta();

            if (meta != null) {
                meta.setCustomModelData(value);
            }

            product.setItemMeta(meta);
        }
    }

    private void applyLore(@Nullable Map<String, String> replacements) {
        List<String> loreConfig = this.configurationFile.getStringList(configLocation + "lore");
        if (loreConfig.isEmpty()) return;

        ItemMeta meta = product.getItemMeta();
        if (meta == null) return;

        AbstractMessage lore = EvenMoreFish.getAdapter().createMessage(loreConfig);
        lore.setVariables(replacements);

        meta.setLore(lore.getLegacyListMessage());
        product.setItemMeta(meta);
    }

    /**
     * Applies a custom display name to the item, this is if server owners don't like the default colour or whatever their
     * reason is.
     */
    private void applyDisplayName(@Nullable Map<String, String> replacements) {
        String displayName = this.configurationFile.getString(configLocation + "item.displayname");

        if (displayName == null && this.displayName != null) displayName = this.displayName;

        if (displayName != null) {
            ItemMeta meta = product.getItemMeta();

            if (meta != null) {
                if (displayName.isEmpty()) {
                    meta.setDisplayName("");
                } else {
                    AbstractMessage display = EvenMoreFish.getAdapter().createMessage(displayName);
                    display.setVariables(replacements);
                    meta.setDisplayName(display.getLegacyMessage());
                }
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
        String potionSettings = this.configurationFile.getString(configLocation + "item.potion");

        if (potionSettings == null) return;
        if (!(product.getItemMeta() instanceof PotionMeta meta)) return;

        String[] split = potionSettings.split(":");
        if (split.length != 3) {
            EvenMoreFish.getInstance().getLogger().severe(configLocation + "item.potion: is formatted incorrectly in the fish.yml file. Use \"potion:duration:amplifier\".");
        }

        try {
            meta.addCustomEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(split[0])), Integer.parseInt(split[1]) * 20, Integer.parseInt(split[2]) - 1, false), true);
        } catch (NumberFormatException exception) {
            EvenMoreFish.getInstance().getLogger().severe(configLocation + "item.potion: is formatted incorrectly in the fish.yml file. Use \"potion:duration:amplifier\", where duration & amplifier are integer values.");
        } catch (NullPointerException exception) {
            EvenMoreFish.getInstance().getLogger().severe(configLocation + "item.potion: " + split[0] + " is not a valid potion name. A list can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
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

    public void enableDefaultChecks() {
        setItemModelDataCheck(true);
        setItemDamageCheck(true);
        setItemDyeCheck(true);
        setItemGlowCheck(true);
        setPotionMetaCheck(true);
    }

    public void enableAllChecks() {
        setItemDisplayNameCheck(true);
        setItemLoreCheck(true);
        setItemDamageCheck(true);
        setItemDyeCheck(true);
        setItemGlowCheck(true);
        setItemModelDataCheck(true);
        setPotionMetaCheck(true);
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

    public void setItemLoreCheck(boolean itemLoreCheck) {
        this.itemLoreCheck = itemLoreCheck;
    }

    public void setPotionMetaCheck(boolean metaPotionCheck) {
        this.itemPotionMetaCheck = metaPotionCheck;
    }

    public Material getMaterial() {
        return product.getType();
    }

    public int getChosenRandomIndex() {
        return chosenRandomIndex;
    }

    public boolean isRawMaterial() {
        return rawMaterial;
    }
}