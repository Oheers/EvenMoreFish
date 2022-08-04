package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.requirements.Requirement;
import com.oheers.fish.selling.WorthNBT;
import com.oheers.fish.utils.ItemFactory;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    List<Requirement> requirements = new ArrayList<>();

    boolean wasBaited;

    List<String> allowedRegions;

    double weight;

    double minSize, maxSize;

    boolean isCompExemptFish;

    boolean disableFisherman;

    public Fish(Rarity rarity, String name) throws InvalidFishException {
        this.rarity = rarity;
        this.name = name;
        this.weight = 0;
        this.disableFisherman = EvenMoreFish.fishFile.getConfig().getBoolean(
                "fish." + this.rarity.getValue() + "." + this.name + ".disable-fisherman",
                EvenMoreFish.raritiesFile.getConfig().getBoolean("rarities." + this.rarity.getValue() + ".disable-fisherman", false)
        );

        if (rarity == null) throw new InvalidFishException(name + " could not be fetched from the config.");

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

    /**
     * Returns the item stack version of the fish to be given to the player.
     *
     * @param randomIndex If the value isn't -1 then a specific index of the random results of the fish will be chosen,
     *                    relying on the fact that it's a fish doing random choices. If it is -1 then a random one will
     *                    be rolled.
     * @return An ItemStack version of the fish.
     */
    public ItemStack give(int randomIndex) {

        ItemStack fish = factory.createItem(getFishermanPlayer(), randomIndex);
        if (factory.isRawMaterial()) return fish;
        ItemMeta fishMeta;

        if ((fishMeta = fish.getItemMeta()) != null) {
            if (displayName != null) fishMeta.setDisplayName(FishUtils.translateHexColorCodes(displayName));
            else fishMeta.setDisplayName(FishUtils.translateHexColorCodes(rarity.getColour() + name));

            fishMeta.setLore(getFishLore());

            fishMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            fishMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            fishMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            fish.setItemMeta(fishMeta);

            fish = WorthNBT.setNBT(fish, this);
        }

        return fish;
    }

    private OfflinePlayer getFishermanPlayer() {
        return fisherman == null ? null : Bukkit.getOfflinePlayer(fisherman);
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
            int len = (int) (Math.random() * (maxSize * 10 - minSize * 10 + 1) + minSize * 10);
            this.length = (float) len / 10;
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
        if (separated.length != 3)
            Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));

        PotionEffectType effect = PotionEffectType.getByName(separated[0].toUpperCase());
        int amplitude = Integer.parseInt(separated[1]);
        // *20 to bring it to seconds rather than ticks
        int time = Integer.parseInt(separated[2]) * 20;

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

    /**
     * From the new method of fetching the lore, where the admin specifies exactly how they want the lore to be set up,
     * letting them modify the order, add a twist to how they want extra details and so on.
     * <p>
     * It goes through each line of the Messages' getFishLoreFormat, if the line is just {fish_lore} then it gets replaced
     * with a fish's lore value, if not then nothing is done.
     *
     * @return A lore to be used by fetching data from the old messages.yml set-up.
     */
    private List<String> getFishLore() {
        Message newLoreLine = new Message(ConfigMessage.FISH_LORE);
        newLoreLine.setRarityColour(rarity.getColour());

        newLoreLine.addLore(
            "{fish_lore}",
            EvenMoreFish.fishFile.getConfig().getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore")
        );

        newLoreLine.addLore(
            "{fisherman_lore}",
                !disableFisherman && getFishermanPlayer() != null ?
                EvenMoreFish.msgs.config.getStringList("fisherman-lore")
                : null
        );

        if (!disableFisherman && getFishermanPlayer() != null) newLoreLine.setPlayer(getFishermanPlayer().getName());

        newLoreLine.addLore(
            "{length_lore}",
            length > 0 ?
                EvenMoreFish.msgs.config.getStringList("length-lore")
                : null
        );

        if (length > 0) newLoreLine.setLength(Float.toString(length));

        if (rarity.getDisplayName() != null) newLoreLine.setRarity(rarity.getDisplayName());
        else newLoreLine.setRarity(this.rarity.getLorePrep());

        List<String> newLore = Arrays.asList(newLoreLine.getRawMessage(true, true).split("\n"));
        if (getFishermanPlayer() != null && EvenMoreFish.usingPAPI) {
            return newLore.stream().map(l -> PlaceholderAPI.setPlaceholders(getFishermanPlayer(), l)).collect(Collectors.toList());
        }

        return newLore;
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

    public boolean hasFishermanDisabled() {
        return disableFisherman;
    }

    public UUID getFisherman() {
        return fisherman;
    }

    public void setFisherman(UUID fisherman) {
        this.fisherman = fisherman;
    }

    public boolean isCompExemptFish() {
        return isCompExemptFish;
    }

    public void setCompExemptFish(boolean compExemptFish) {
        isCompExemptFish = compExemptFish;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public Float getLength() {
        return length;
    }

    public void setLength(Float length) {
        this.length = length == null ? -1 : length;
    }

    public List<Reward> getActionRewards() {
        return actionRewards;
    }

    public List<Reward> getFishRewards() {
        return fishRewards;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemFactory getFactory() {
        return factory;
    }

    public List<Requirement> getRequirements() {
        return this.requirements;
    }

    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public boolean isWasBaited() {
        return wasBaited;
    }

    public void setWasBaited(boolean wasBaited) {
        this.wasBaited = wasBaited;
    }
}
