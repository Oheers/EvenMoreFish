package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.Xmas2022Config;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.requirements.Requirement;
import com.oheers.fish.selling.WorthNBT;
import com.oheers.fish.utils.ItemFactory;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
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
    List<Reward> sellRewards;
    String eventType;

    List<Requirement> requirements = new ArrayList<>();

    boolean wasBaited;
    boolean silent;

    List<String> allowedRegions;

    double weight;

    double minSize;
    double maxSize;

    boolean isCompExemptFish;

    boolean disableFisherman;

    boolean xmasFish;
    FileConfiguration fishConfig;
    FileConfiguration rarityConfig;

    private int day = -1;

    public Fish(Rarity rarity, String name, boolean isXmas2022Fish) throws InvalidFishException {
        if (rarity == null) {
            throw new InvalidFishException(name + " could not be fetched from the config.");
        }
        this.rarity = rarity;
        this.name = name;
        this.weight = 0;
        this.length = -1F;
        this.xmasFish = isXmas2022Fish;
        this.setFishAndRarityConfig();
        final boolean defaultRarityDisableFisherman = RaritiesFile.getInstance().getConfig().getBoolean("rarities." + this.rarity.getValue() + ".disable-fisherman", false);
        this.disableFisherman = this.fishConfig.getBoolean("fish." + this.rarity.getValue() + "." + this.name + ".disable-fisherman", defaultRarityDisableFisherman);

        this.factory = new ItemFactory("fish." + this.rarity.getValue() + "." + this.name, this.xmasFish);
        checkDisplayName();

        // These settings don't mean these will be applied, but they will be considered if the settings exist.
        factory.enableDefaultChecks();
        factory.setItemDisplayNameCheck(this.displayName != null);
        factory.setItemLoreCheck(!this.fishConfig.getBoolean("fish." + this.rarity.getValue() + "." + this.name + ".disable-lore", false));

        setSize();
        checkEatEvent();
        checkIntEvent();
        checkDisplayName();
        checkSilent();

        checkFishEvent();

        checkSellEvent();
    }
    
    /*
      Accounts for bug https://github.com/Oheers/EvenMoreFish/issues/173
      This will allow breaking heads that have already been placed with the wrong nbt data.
     */
    private void setFishAndRarityConfig() {
        if (this.xmasFish && Xmas2022Config.getInstance().getConfig() != null) {
            this.fishConfig = Xmas2022Config.getInstance().getConfig();
            this.rarityConfig = Xmas2022Config.getInstance().getConfig();
        } else {
            this.xmasFish = false;
            this.fishConfig = FishFile.getInstance().getConfig();
            this.rarityConfig = RaritiesFile.getInstance().getConfig();
        }
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

            if (!this.fishConfig.getBoolean("fish." + this.rarity.getValue() + "." + this.name + ".disable-lore", false)) {
                fishMeta.setLore(getFishLore());
            }

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
        this.minSize = this.fishConfig.getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.minSize");
        this.maxSize = this.fishConfig.getDouble("fish." + this.rarity.getValue() + "." + this.name + ".size.maxSize");

        // are min & max size changed? If not, there's no fish-specific value. Check the rarity's value
        if (minSize == 0.0 && maxSize == 0.0) {
            this.minSize = this.rarityConfig.getDouble("rarities." + this.rarity.getValue() + ".size.minSize");
            this.maxSize = this.rarityConfig.getDouble("rarities." + this.rarity.getValue() + ".size.maxSize");
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
        return !fishRewards.isEmpty();
    }

    public boolean hasSellRewards() {
        return !sellRewards.isEmpty();
    }

    public boolean hasIntRewards() {
        if (eventType != null) {
            return eventType.equals("int");
        } else return false;
    }

    // checks if the config contains a message to be displayed when the fish is fished
    private void checkMessage() {
        String msg = this.fishConfig.getString("fish." + this.rarity.getValue() + "." + this.name + ".message");

        if (msg != null) {
            if (Bukkit.getPlayer(fisherman) != null) {
                Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).sendMessage(FishUtils.translateHexColorCodes(msg));
            }
        }
    }

    private void checkEffects() {

        String effectConfig = this.fishConfig.getString("fish." + this.rarity.getValue() + "." + this.name + ".effect");

        // if the config doesn't have an effect stated to be given
        if (effectConfig == null) return;

        String[] separated = effectConfig.split(":");
        // if it's formatted wrong, it'll just give the player this as a stock effect
        if (separated.length < 3) {
            Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
            EvenMoreFish.getInstance().getLogger().warning("Invalid potion effect specified. Defaulting to Speed 2 for 5 seconds.");
            return;
        }

        PotionEffectType effect = PotionEffectType.getByName(separated[0].toUpperCase());
        int amplitude = Integer.parseInt(separated[1]);
        // *20 to bring it to seconds rather than ticks
        int time = Integer.parseInt(separated[2]) * 20;

        try {
            Objects.requireNonNull(Bukkit.getPlayer(this.fisherman)).addPotionEffect(new PotionEffect(effect, time, amplitude));
        } catch (IllegalArgumentException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "ATTENTION! There was an error adding the effect from the " + this.name + " fish.");
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "ATTENTION! Check your config files and ensure spelling of the effect name is correct.");
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "ATTENTION! If the problem persists, ask for help on the support discord server.");
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
        List<String> loreOverride = this.fishConfig.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore-override");
        Message newLoreLine;
        if (!loreOverride.isEmpty()) {
            newLoreLine = new Message(loreOverride);
        } else {
            newLoreLine = new Message(ConfigMessage.FISH_LORE);
        }
        newLoreLine.setRarityColour(rarity.getColour());

        newLoreLine.addLore(
                "{fish_lore}",
                this.fishConfig.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".lore")
        );

        newLoreLine.setVariable("{fisherman_lore}",
            !disableFisherman && getFishermanPlayer() != null ?
                (new Message(ConfigMessage.FISHERMAN_LORE)).message
                : ""
        );

        if (!disableFisherman && getFishermanPlayer() != null) newLoreLine.setPlayer(getFishermanPlayer().getName());

        newLoreLine.setVariable("{length_lore}",
            length > 0 ?
                (new Message(ConfigMessage.LENGTH_LORE)).message
                : ""
        );

        if (length > 0) newLoreLine.setLength(Float.toString(length));

        if (rarity.getDisplayName() != null) newLoreLine.setRarity(rarity.getDisplayName());
        else newLoreLine.setRarity(this.rarity.getLorePrep());

        List<String> newLore = Arrays.asList(newLoreLine.getRawMessage(true).split("\n"));
        if (getFishermanPlayer() != null && EvenMoreFish.getInstance().isUsingPAPI()) {
            return newLore.stream().map(l -> PlaceholderAPI.setPlaceholders(getFishermanPlayer(), l)).collect(Collectors.toList());
        }

        return newLore;
    }

    public void checkDisplayName() {
        this.displayName = this.fishConfig.getString("fish." + this.rarity.getValue() + "." + this.name + ".displayname");
    }

    public void checkEatEvent() {
        List<String> configRewards = this.fishConfig.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".eat-event");
        // Checks if the player has actually set rewards for an eat event
        if (!configRewards.isEmpty()) {
            // Informs the main class to load up an PlayerItemConsumeEvent listener
            EvenMoreFish.getInstance().setCheckingEatEvent(true);
            this.eventType = "eat";
            actionRewards = new ArrayList<>();

            // Translates all the rewards into Reward objects and adds them to the fish.
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.actionRewards.add(new Reward(reward));
            });
        }
    }

    public void checkFishEvent() {
        fishRewards = new ArrayList<>();
        List<String> configRewards = this.fishConfig.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".catch-event");
        if (!configRewards.isEmpty()) {
            // Translates all the rewards into Reward objects and adds them to the fish.
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.fishRewards.add(new Reward(reward));
            });
        }
    }

    public void checkSellEvent() {
        sellRewards = new ArrayList<>();
        List<String> configRewards = this.fishConfig.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".sell-event");
        if (!configRewards.isEmpty())  {
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.sellRewards.add(new Reward(reward));
            });
        }
    }

    public void checkIntEvent() {
        List<String> configRewards = this.fishConfig.getStringList("fish." + this.rarity.getValue() + "." + this.name + ".interact-event");
        // Checks if the player has actually set rewards for an interact event
        if (!configRewards.isEmpty()) {
            // Informs the main class to load up an PlayerItemConsumeEvent listener
            EvenMoreFish.getInstance().setCheckingIntEvent(true);
            this.eventType = "int";
            actionRewards = new ArrayList<>();

            // Translates all the rewards into Reward objects and adds them to the fish.
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.actionRewards.add(new Reward(reward));
            });
        }
    }

    /**
     * Checks if the fish has silent: true enabled, which stops the "You caught ... fish" from being broadcasted to anyone.
     */
    public void checkSilent() {
        this.silent = this.fishConfig.getBoolean("fish." + this.rarity.getValue() + "." + this.name + ".silent", false);
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

    public List<Reward> getSellRewards() { return sellRewards; }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return name;
        }
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

    public void addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public boolean isWasBaited() {
        return wasBaited;
    }

    public void setWasBaited(boolean wasBaited) {
        this.wasBaited = wasBaited;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDay() {
        return day;
    }

    public boolean isXmasFish() {
        return xmasFish;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public String parseEventPlaceholders(String rewardString) {

        // {length} Placeholder
        rewardString = rewardString.replace("{length}", String.valueOf(length));

        // {rarity} Placeholder
        String rarityReplacement = "";
        if (rarity != null) {
            rarityReplacement = rarity.getValue();
        }
        rewardString = rewardString.replace("{rarity}", rarityReplacement);

        // {displayname} Placeholder
        String displayNameReplacement = "";
        if (displayName != null) {
            displayNameReplacement = displayName;
        }
        rewardString = rewardString.replace("{displayname}", displayNameReplacement);

        // {name} Placeholder
        String nameReplacement = "";
        if (name != null) {
            nameReplacement = name;
        }
        rewardString = rewardString.replace("{name}", nameReplacement);

        return rewardString;
    }

}
