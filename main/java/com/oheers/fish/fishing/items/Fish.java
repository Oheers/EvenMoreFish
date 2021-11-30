package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.selling.WorthNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public class Fish implements Cloneable {

    String name;
    Rarity rarity;
    ItemStack type;
    UUID fisherman;
    Float length;

    String displayName;

    String dyeColour;

    List<Reward> actionRewards;
    List<Reward> fishRewards;
    String eventType;

    List<Biome> biomes;
    List<String> allowedRegions;

    String permissionNode;

    double weight;

    double minSize, maxSize;

    boolean glowing;

    FileConfiguration configurationFile, rarityConfiguration;

    public Fish(Rarity rarity, String name) {
        this.configurationFile = FishUtils.findConfigFile(rarity.getValue());
        this.rarityConfiguration = FishUtils.findRarityFile(rarity.getValue());

        this.rarity = rarity;
        this.name = name;
        this.type = setType();
        this.weight = 0;

        setSize();
        checkEatEvent();
        checkIntEvent();
        checkDisplayName();

        checkDye();

        fishRewards = new ArrayList<>();
        checkFishEvent();
    }

    public ItemStack give() {

        ItemStack fish = this.type;

        if (glowing) fish.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ItemMeta fishMeta = fish.getItemMeta();

        if (displayName != null) fishMeta.setDisplayName(FishUtils.translateHexColorCodes(displayName));
        else fishMeta.setDisplayName(FishUtils.translateHexColorCodes(rarity.getColour() + name));

        fishMeta.setLore(generateLore());

        if (glowing) fishMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        if (dyeColour != null) {
            try {
                LeatherArmorMeta meta = (LeatherArmorMeta) fishMeta;

                Color colour = Color.decode(dyeColour);

                meta.setColor(org.bukkit.Color.fromRGB(colour.getRed(), colour.getGreen(), colour.getBlue()));
                meta.addItemFlags(ItemFlag.HIDE_DYE);
                fish.setItemMeta(meta);
            } catch (ClassCastException exception) {
                EvenMoreFish.logger.log(Level.SEVERE, "Could not add hex value: " + dyeColour + " to " + name + ". Item is likely not a leather material.");
            }
        }

        fishMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        fish.setItemMeta(fishMeta);

        WorthNBT.setNBT(fish, this.length, this.getRarity().getValue(), this.getName());
        addModelData(fish);

        return fish;
    }

    private void setSize() {
        this.minSize = configurationFile.getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.minSize");
        this.maxSize = configurationFile.getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.maxSize");

        // are min & max size changed? If not, there's no fish-specific value. Check the rarity's value
        if (minSize == 0.0 && maxSize == 0.0) {
            this.minSize = rarityConfiguration.getDouble("rarities." + this.rarity.getValue() + ".size.minSize");
            this.maxSize = rarityConfiguration.getDouble("rarities." + this.rarity.getValue() + ".size.maxSize");
        }

        // If there's no rarity-specific value (or max is smaller than min), to avoid being in a pickle we just set min default to 0 and max default to 10
        if ((minSize == 0.0 && maxSize == 0.0) || minSize > maxSize) {
            this.minSize = 0.0;
            this.maxSize = 10.0;
        }
    }

    private void generateSize() {
        if (minSize < 0) {
            this.length = -1f;
        } else {
            // Random logic that returns a float to 1dp
            int len = (int) (Math.random() * (maxSize*10 - minSize*10 + 1) + minSize*10);
            this.length = (float) len/10;
        }
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setFisherman(UUID fisherman) {
        this.fisherman = fisherman;
    }

    public void setLength(Float length) {
        this.length = length;
    }

    public Float getLength() {
        return length;
    }

    public List<Reward> getActionRewards() {
        return actionRewards;
    }

    public List<Reward> getFishRewards() {
        return fishRewards;
    }

    public boolean hasEatRewards() {
        if (eventType != null) {
            return eventType.equals("eat");
        } else return false;
    }

    public boolean hasFishRewards() {
        return fishRewards.size() != 0;
    }

    public boolean hasIntRewards() {
        if (eventType != null) {
            return eventType.equals("int");
        } else return false;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public FileConfiguration getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(FileConfiguration configurationFile) {
        this.configurationFile = configurationFile;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public void setPermissionNode(String permissionNode) {
        this.permissionNode = permissionNode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private ItemStack setType() {

        String uValue = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".item.head-uuid");
        // The fish has item: uuid selected
        // note - only works for players who have joined the server previously, not sure if this'll make it to release.
        if (uValue != null) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uValue)));
            skull.setItemMeta(meta);
            return skull;
        }

        // The fish has item: 64 selected
        String bValue = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".item.head-64");
        if (bValue != null) {
            return FishUtils.get(bValue);
        }

        // The fish has item: material selected
        String mValue = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".item.material");
        if (mValue != null) {

            Material m = Material.getMaterial(mValue.toUpperCase());
            if (m == null) {
                EvenMoreFish.logger.log(Level.SEVERE, this.name + " has failed to load material: " + mValue);
                m = Material.COD;
            }

            return new ItemStack(m);
        }

        // The fish has no item type specified
        else {
            return new ItemStack(Material.COD);
        }
    }

    public ItemStack getType() {
        return type;
    }

    // checks if the config contains a message to be displayed when the fish is fished
    private void checkMessage() {
        String msg = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".message");

        if (msg != null) {
            if (Bukkit.getPlayer(fisherman) != null) {
                Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).sendMessage(FishUtils.translateHexColorCodes(msg));
            }
        }

    }

    private void checkEffects() {

        String effectConfig = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".effect");

        // if the config doesn't have an effect stated to be given
        if (effectConfig == null) return;

        String[] separated = effectConfig.split(":");
        // if it's formatted wrong, it'll just give the player this as a stock effect
        if (separated.length != 3) Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));

        PotionEffectType effect = PotionEffectType.getByName(separated[0].toUpperCase());
        int amplitude = Integer.parseInt(separated[1]);
        // *20 to bring it to seconds rather than ticks
        int time = Integer.parseInt(separated[2])*20;

        try {
            Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).addPotionEffect(new PotionEffect(effect, time, amplitude));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Bukkit.getServer().getLogger().log(Level.SEVERE, "ATTENTION! There was an error adding the effect from the " + this.name + " fish.");
            Bukkit.getServer().getLogger().log(Level.SEVERE, "ATTENTION! Check your config files and ensure spelling of the effect name is correct.");
            Bukkit.getServer().getLogger().log(Level.SEVERE, "ATTENTION! If the problem persists, ask for help on the support discord server.");
        }

    }

    public void setBiomes(List<Biome> biomes) {
        this.biomes = biomes;
    }

    public List<Biome> getBiomes() {
        return biomes;
    }

    public List<String> getAllowedRegions() {
        return allowedRegions;
    }

    public void setAllowedRegions(List<String> allowedRegions) {
        this.allowedRegions = allowedRegions;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    // prepares it to be given to the player
    public void init() {

        generateSize();
        checkMessage();
        checkEffects();
    }

    private List<String> generateLore() {

        // standard lore
        List<String> lore = new ArrayList<>();

        if (fisherman != null) {
            lore.add(new Message().setMSG(EvenMoreFish.msgs.fishCaughtBy()).setPlayer(Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).getName()).toString());
        }

        if (this.length != -1) lore.add(new Message().setMSG(EvenMoreFish.msgs.fishLength()).setLength(Float.toString(length)).setRarityColour("").toString());
        lore.add(" ");

        // custom lore in fish.yml
        List<String> potentialLore = configurationFile.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore");

        // checks that the custom lore exists, then adds it on to the lore
        if (potentialLore.size() > 0) {
            // does colour coding, hence why .addAll() isn't used
            for (String line : potentialLore) {
                lore.add(FishUtils.translateHexColorCodes(line));
            }
        }

        // a little footer showing the rarity
        lore.add(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getRarityPrefix()) + FishUtils.translateHexColorCodes(this.rarity.getLorePrep()));

        return lore;
    }

    public void checkDisplayName() {
        this.displayName = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".displayname");
    }

    public void checkDye() {
        this.dyeColour = configurationFile.getString("fish." + this.rarity.getValue() + "." + this.name + ".dye-colour");
    }

    public void randomBreak() {
        Damageable nonDamaged = (Damageable) type.getItemMeta();

        // checking if the user has already set this item to have durability in fish.yml (possible future update)
        //if (nonDamaged.hasDamage()) return;

        int min = nonDamaged.getDamage();
        int max = this.type.getType().getMaxDurability();

        nonDamaged.setDamage((int) (Math.random() * (max - min + 1) + min));
        type.setItemMeta((ItemMeta) nonDamaged);
    }

    public void checkEatEvent() {
        List<String> configRewards = configurationFile.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".eat-event");
        // Checks if the player has actually set rewards for an eat event
        if (!configRewards.isEmpty()) {
            // Informs the main class to load up an PlayerItemConsumeEvent listener
            EvenMoreFish.checkingEatEvent = true;
            this.eventType = "eat";
            actionRewards = new ArrayList<>();

            // Translates all the rewards into Reward objects and adds them to the fish.
            for (String reward : configRewards) {
                this.actionRewards.add(new Reward(reward));
            }
        }
    }

    public void checkFishEvent() {
        List<String> configRewards = configurationFile.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".catch-event");
        if (!configRewards.isEmpty()) {
            // Translates all the rewards into Reward objects and adds them to the fish.
            for (String reward : configRewards) {
                this.fishRewards.add(new Reward(reward));
            }
        }
    }

    public void checkIntEvent() {
        List<String> configRewards = configurationFile.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".interact-event");
        // Checks if the player has actually set rewards for an interact event
        if (!configRewards.isEmpty()) {
            // Informs the main class to load up an PlayerItemConsumeEvent listener
            EvenMoreFish.checkingIntEvent = true;
            this.eventType = "int";
            actionRewards = new ArrayList<>();

            // Translates all the rewards into Reward objects and adds them to the fish.
            for (String reward : configRewards) {
                this.actionRewards.add(new Reward(reward));
            }
        }
    }

    private void addModelData(ItemStack fish) {
        int value = configurationFile.getInt("fish." + this.rarity.getValue() + "." + this.name + ".item.custom-model-data");
        if (value != 0) {
            ItemMeta meta = fish.getItemMeta();
            meta.setCustomModelData(value);
            fish.setItemMeta(meta);
        }
    }

    @Override
    public Fish clone() throws CloneNotSupportedException {
        return (Fish) super.clone();
    }

    public ItemStack preview() {
        ItemStack previewFish = type;
        ItemMeta previewMeta = previewFish.getItemMeta();

        // standard lore
        List<String> lore = new ArrayList<>();

        lore.add(new Message().setMSG(EvenMoreFish.msgs.fishCaughtBy()).setPlayer("Player").toString());
        lore.add(new Message().setMSG(ChatColor.WHITE + Double.toString(minSize) + " to " + maxSize).toString());
        lore.add(" ");

        // custom lore in fish.yml
        List<String> potentialLore = configurationFile.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore");

        // checks that the custom lore exists, then adds it on to the lore
        if (potentialLore.size() > 0) {
            // does colour coding, hence why .addAll() isn't used
            for (String line : potentialLore) {
                lore.add(FishUtils.translateHexColorCodes(line));
            }
        }

        lore.add(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getRarityPrefix()) + FishUtils.translateHexColorCodes(this.rarity.getLorePrep()));

        previewMeta.setDisplayName(FishUtils.translateHexColorCodes(rarity.getColour() + name));
        previewMeta.setLore(lore);

        previewFish.setItemMeta(previewMeta);
        return previewFish;
    }

    public UUID getFisherman() {
        return fisherman;
    }
}
