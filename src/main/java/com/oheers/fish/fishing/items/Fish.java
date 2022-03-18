package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.selling.WorthNBT;
import com.oheers.fish.utils.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class Fish implements Cloneable {

    String name;
    Rarity rarity;
    ItemFactory factory;
    UUID fisherman;
    Float length;

    String displayName;

    List<Reward> actionRewards;
    List<Reward> fishRewards;
    String eventType;

    List<Biome> biomes;
    List<String> allowedRegions;

    String permissionNode;

    double weight;

    double minSize, maxSize;

    boolean isCompExemptFish;

    public Fish(Rarity rarity, String name) {
        this.rarity = rarity;
        this.name = name;
        this.weight = 0;

        this.factory = new ItemFactory("fish." + this.rarity.getValue() + "." + this.name);
        checkDisplayName();

        // These settings don't mean these will be applied, but they will be considered if the settings exist.
        factory.setItemModelDataCheck(true);
        factory.setItemDamageCheck(true);
        factory.setItemDisplayNameCheck(this.displayName != null);
        factory.setItemDyeCheck(true);
        factory.setItemGlowCheck(true);
        factory.setPotionMetaCheck(true);

        setSize();
        checkEatEvent();
        checkIntEvent();
        checkDisplayName();

        fishRewards = new ArrayList<>();
        checkFishEvent();
    }

    public ItemStack give() {

        ItemStack fish = factory.createItem();

        ItemMeta fishMeta = fish.getItemMeta();

        if (displayName != null) fishMeta.setDisplayName(FishUtils.translateHexColorCodes(displayName));
        else fishMeta.setDisplayName(FishUtils.translateHexColorCodes(rarity.getColour() + name));

        fishMeta.setLore(generateLore());

        fishMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        fishMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        fish.setItemMeta(fishMeta);

        WorthNBT.setNBT(fish, this.length, this.getRarity().getValue(), this.getName());

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
        if (minSize < 0) {
            this.length = -1f;
        } else {
            // Random logic that returns a float to 1dp
            int len = (int) (Math.random() * (maxSize*10 - minSize*10 + 1) + minSize*10);
            this.length = (float) len/10;
        }
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

    // checks if the config contains a message to be displayed when the fish is fished
    private void checkMessage() {
        String msg = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".message");

        if (msg != null) {
            if (Bukkit.getPlayer(fisherman) != null) {
                Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).sendMessage(FishUtils.translateHexColorCodes(msg));
            }
        }
    }

    private void checkEffects() {

        String effectConfig = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".effect");

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

    // prepares it to be given to the player
    public void init() {

        generateSize();
        checkMessage();
        checkEffects();
    }

    private List<String> generateLore() {
        if (EvenMoreFish.msgs.getFishLoreFormat().isEmpty()) {
            return getFromOldLore();
        } else {
            return getFromNewLore();
        }
    }

    /**
     * From the old lore generation where the messages.yml asked users for the caught-by, length and rarity on separate
     * lines and then merged it together manually, which was very limiting for servers that wanted fully stackable
     * fish.
     *
     * The fisherman is checked to be null, if not then the {player} is replaced with their name. The length is then checked
     * to be displayed, if it is it replaces {length}. A gap is added then the potential lore for the fish is fetched,
     * the rarity is applied as a footer then the whole thing is returned.
     *
     * @return A lore to be used by fetching data from the old messages.yml set-up.
     */
    private List<String> getFromOldLore() {
        List<String> lore = new ArrayList<>();

        if (fisherman != null) {
            lore.add(new Message().setMSG(EvenMoreFish.msgs.fishCaughtBy()).setPlayer(Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).getName()).toString());
        }

        if (this.length != -1) lore.add(new Message().setMSG(EvenMoreFish.msgs.fishLength()).setLength(Float.toString(length)).setRarityColour("").toString());
        lore.add(" ");

        applyCustomLore(lore);

        // a little footer showing the rarity
        lore.add(FishUtils.translateHexColorCodes(EvenMoreFish.msgs.getRarityPrefix()) + FishUtils.translateHexColorCodes(this.rarity.getLorePrep()));
        return lore;
    }

    /**
     * From the new method of fetching the lore, where the admin specifies exactly how they want the lore to be set up,
     * letting them modify the order, add a twist to how they want extra details and so on.
     *
     * It goes through each line of the Messages' getFishLoreFormat, if the line is just {fish_lore} then it gets replaced
     * with a fish's lore value, if not then nothing is done.
     *
     * @return A lore to be used by fetching data from the old messages.yml set-up.
     */
    private List<String> getFromNewLore() {
        List<String> resultantLore = new ArrayList<>();

        for (String line : EvenMoreFish.msgs.getFishLoreFormat()) {
            if (line.equals("{fish_lore}")) {
                applyCustomLore(resultantLore);
            } else {
                Message loreLine = new Message()
                        .setMSG(line)
                        .setRarityColour(rarity.getColour())
                        .setPlayer(Bukkit.getOfflinePlayer(fisherman).getName())
                        .setLength(Float.toString(length));
                if (rarity.getDisplayName() != null) loreLine.setRarity(rarity.getDisplayName());
                else loreLine.setRarity(rarity.getValue().toUpperCase());

                resultantLore.add(loreLine.toString());
            }
        }

        return resultantLore;
    }

    /**
     * Converts the lore option of the fish into a formatted version using the FishUtils#translateHexCode method.
     * It's then added to the inputLore
     *
     * @param inputLore The current lore you want to ADD the custom lore to.
     */
    private void applyCustomLore(List<String> inputLore) {
        // custom lore in fish.yml
        List<String> potentialLore = EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore");

        // checks that the custom lore exists, then adds it on to the lore
        if (potentialLore.size() > 0) {
            // does colour coding, hence why .addAll() isn't used
            for (String line : potentialLore) {
                inputLore.add(FishUtils.translateHexColorCodes(line));
            }
        }
    }

    public void checkDisplayName() {
        this.displayName = EvenMoreFish.fishFile.getConfig().getString("fish." + this.rarity.getValue() + "." + this.name + ".displayname");
    }

    public void checkEatEvent() {
        List<String> configRewards = EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".eat-event");
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
        List<String> configRewards = EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".catch-event");
        if (!configRewards.isEmpty()) {
            // Translates all the rewards into Reward objects and adds them to the fish.
            for (String reward : configRewards) {
                this.fishRewards.add(new Reward(reward));
            }
        }
    }

    public void checkIntEvent() {
        List<String> configRewards = EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".interact-event");
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

    @Override
    public Fish clone() throws CloneNotSupportedException {
        return (Fish) super.clone();
    }

    public UUID getFisherman() {
        return fisherman;
    }

    public boolean isCompExemptFish() {
        return isCompExemptFish;
    }

    public void setCompExemptFish(boolean compExemptFish) {
        isCompExemptFish = compExemptFish;
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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
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

    public ItemFactory getFactory() {
        return factory;
    }
}
