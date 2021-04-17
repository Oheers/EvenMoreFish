package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.selling.WorthNBT;
import dev.dbassett.skullcreator.SkullCreator;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class Fish {

    String name;
    Rarity rarity;
    ItemStack type;
    Player fisherman;
    Float length;

    List<Reward> eatRewards;

    List<Biome> biomes;

    double minSize, maxSize;

    public Fish(Rarity rarity, String name) {
        this.rarity = rarity;
        this.name = name;
        this.type = setType();

        setSize();
        checkEatEvent();
    }

    public ItemStack give(Player fisherman) {

        ItemStack fish = this.type;
        ItemMeta fishMeta = fish.getItemMeta();

        fishMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rarity.getColour() + name));
        fishMeta.setLore(generateLore());
        fish.setItemMeta(fishMeta);

        WorthNBT.setNBT(fish, this.length, this.getRarity().getValue(), this.getName());
        addModelData(fish);

        return fish;
    }

    private void setSize() {
        this.minSize = EvenMoreFish.fishFile.getConfig().getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.minSize");
        this.maxSize = EvenMoreFish.fishFile.getConfig().getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.maxSize");

        // are min & max size changed? If not, there's no fish-specific value. Check the rarity's value
        if (minSize == 0.0 && maxSize == 0.0) {
            this.minSize = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + this.rarity.getValue() + ".size.minSize");
            this.maxSize = EvenMoreFish.raritiesFile.getConfig().getDouble("rarities." + this.rarity.getValue() + ".size.maxSize");
        }

        // If there's no rarity-specific value (or max is smaller than min), to avoid being in a pickle we just set min default to 0 and max default to 10
        if ((minSize == 0.0 && maxSize == 0.0) || minSize > maxSize) {
            this.minSize = 0.0;
            this.maxSize = 10.0;
        }
    }

    private void generateSize() {
        // Random logic that returns a float to 1dp
        int len = (int) (Math.random() * (maxSize*10 - minSize*10 + 1) + minSize*10);
        this.length = (float) len/10;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setFisherman(Player fisherman) {
        this.fisherman = fisherman;
    }

    public void setLength(Float length) {
        this.length = length;
    }

    public Float getLength() {
        return length;
    }

    public List<Reward> getEatRewards() {

        return eatRewards;
    }

    public boolean hasEatRewards() { return eatRewards != null;}

    private ItemStack setType() {

        String uValue = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".item.head-uuid");
        // The fish has item: uuid selected
        // note - only works for players who have joined the server previously, not sure if this'll make it to release.
        if (uValue != null) {
            return SkullCreator.itemWithUuid(new ItemStack(Material.valueOf("PLAYER_HEAD")), UUID.fromString(uValue));
        }

        // The fish has item: 64 selected
        String bValue = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".item.head-64");
        if (bValue != null) {
            return SkullCreator.itemFromBase64(bValue);
        }

        // The fish has item: material selected
        String mValue = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".item.material");
        if (mValue != null) {
            return new ItemStack(Objects.requireNonNull(Material.getMaterial(mValue)));
        }

        // The fish has no item type specified
        else {
            return new ItemStack(Material.COD);
        }
    }

    public ItemStack getType() {
        return type;
    }

    private String format(String length) {
        DecimalFormat df = new DecimalFormat("###,###.#");
        return df.format(Double.parseDouble(length));
    }

    // checks if the config contains a message to be displayed when the fish is fished
    private void checkMessage() {
        String msg = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".message");

        if (msg != null) this.fisherman.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

    }

    private void checkEffects() {

        String effectConfig = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".effect");

        // if the config doesn't have an effect stated to be given
        if (effectConfig == null) return;

        String[] separated = effectConfig.split(":");
        // if it's formatted wrong, it'll just give the player this as a stock effect
        if (separated.length != 3) this.fisherman.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));

        PotionEffectType effect = PotionEffectType.getByName(separated[0].toUpperCase());
        int amplitude = Integer.parseInt(separated[1]);
        // *20 to bring it to seconds rather than ticks
        int time = Integer.parseInt(separated[2])*20;

        try {
            this.fisherman.addPotionEffect(new PotionEffect(effect, time, amplitude));
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

    // prepares it to be given to the player
    public void init() {

        generateSize();
        checkMessage();
        checkEffects();
    }

    private List<String> generateLore() {

        // standard lore
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.WHITE + "Caught by " + fisherman.getName());
        lore.add(ChatColor.WHITE + "Measures " + format(Float.toString(length)) + "cm");
        lore.add(" ");

        // custom lore in fish.yml
        List<String> potentialLore = EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore");

        // checks that the custom lore exists, then adds it on to the lore
        if (potentialLore.size() > 0) {
            // does colour coding, hence why .addAll() isn't used
            for (String line : potentialLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        // a little footer showing the rarity
        lore.add(ChatColor.translateAlternateColorCodes('&', rarity.getColour() + "&l") + rarity.getValue().toUpperCase());

        return lore;
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
        List<String> configRewards = EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".eat-event");
        // Checks if the player has actually set rewards for an eat event
        if (!configRewards.isEmpty()) {
            // Informs the main class to load up an PlayerItemConsumeEvent listener
            EvenMoreFish.checkingEatEvent = true;
            eatRewards = new ArrayList<>();

            // Translates all the rewards into Reward objects and adds them to the fish.
            for (String reward : configRewards) {
                this.eatRewards.add(new Reward(reward));
            }
        }
    }

    private void addModelData(ItemStack fish) {
        int value = EvenMoreFish.fishFile.getConfig().getInt("fish." + this.rarity.getValue() + "." + this.name + ".item.custom-model-data");
        if (value != 0) {
            ItemMeta meta = fish.getItemMeta();
            meta.setCustomModelData(value);
            meta.setDisplayName(Integer.toString(value));
            fish.setItemMeta(meta);
        }
    }
}
