package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.config.FishFile;
import com.oheers.fish.config.RaritiesFile;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.selling.WorthNBT;
import com.oheers.fish.utils.ItemFactory;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Fish implements Cloneable {

    private final @NotNull Section section;
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

    Requirement requirement = new Requirement();

    boolean wasBaited;
    boolean silent;

    double weight;

    double minSize;
    double maxSize;

    boolean isCompExemptFish;

    boolean disableFisherman;

    private int day = -1;

    /**
     * Constructs a Fish from its config section.
     * @param section The section for this fish.
     */
    public Fish(@NotNull Rarity rarity, @Nullable Section section) throws InvalidFishException {
        if (section == null) {
            throw new InvalidFishException("Fish could not be fetched from the config.");
        }
        this.section = section;
        this.rarity = rarity;
        // This should never be null, but we have this check just to be safe.
        this.name = Objects.requireNonNull(section.getNameAsString());

        this.weight = section.getDouble("weight");
        if (this.weight != 0) {
            rarity.setFishWeighted(true);
        }

        this.length = -1F;

        this.disableFisherman = section.getBoolean("disable-fisherman", rarity.isShouldDisableFisherman());

        this.factory = new ItemFactory(null, section);
        checkDisplayName();

        // These settings don't mean these will be applied, but they will be considered if the settings exist.
        factory.enableDefaultChecks();
        factory.setItemDisplayNameCheck(this.displayName != null);
        factory.setItemLoreCheck(!section.getBoolean("disable-lore", false));

        setSize();
        checkEatEvent();
        checkIntEvent();
        checkSilent();

        checkFishEvent();

        checkSellEvent();
        handleRequirements();
    }

    /**
     * Constructs a fish with the provided values.
     * If possible, prefer {@link Fish#Fish(Rarity, Section)} instead.
     */
    public Fish(@NotNull Rarity rarity, @NotNull String name) throws InvalidFishException {
        // Manually obtain the section when this deprecated constructor is used.
        this(rarity, FishFile.getInstance().getConfig().getSection("fish." + rarity.getValue() + "." + name));
    }

    private void handleRequirements() {
        Section requirementSection = section.getSection("requirements");
        requirement = new Requirement();
        if (requirementSection == null) {
            return;
        }
        requirementSection.getRoutesAsStrings(false).forEach(requirementString -> {
            List<String> values = new ArrayList<>();
            if (requirementSection.isList(requirementString)) {
                values.addAll(requirementSection.getStringList(requirementString));
            } else {
                values.add(requirementSection.getString(requirementString));
            }
            requirement.add(requirementString, values);
        });
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
        ItemMeta fishMeta = fish.getItemMeta();

        if (fishMeta != null) {
            NBT.modify(fish, nbt -> {
                nbt.modifyMeta((readOnlyNbt, meta) -> {
                    meta.setDisplayName(FishUtils.translateColorCodes(getDisplayName()));
                    if (!section.getBoolean("disable-lore", false)) {
                        meta.setLore(getFishLore());
                    }
                    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                });
            });

            WorthNBT.setNBT(fish, this);
        }

        return fish;
    }

    private OfflinePlayer getFishermanPlayer() {
        return fisherman == null ? null : Bukkit.getOfflinePlayer(fisherman);
    }

    private void setSize() {
        this.minSize = section.getDouble("size.minSize");
        this.maxSize = section.getDouble("size.maxSize");

        // are min & max size changed? If not, there's no fish-specific value. Check the rarity's value
        if (minSize == 0.0 && maxSize == 0.0) {
            this.minSize = rarity.getMinSize();
            this.maxSize = rarity.getMaxSize();
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
        } else {
            return false;
        }
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
        } else {
            return false;
        }
    }

    // checks if the config contains a message to be displayed when the fish is fished
    private void checkMessage() {
        String msg = section.getString("message");

        if (msg == null) {
            return;
        }
        if (fisherman == null) {
            return;
        }
        Player player = Bukkit.getPlayer(fisherman);
        if (player != null) {
            player.sendMessage(FishUtils.translateColorCodes(msg));
        }
    }

    private void checkEffects() {

        String effectConfig = section.getString("effect");

        // if the config doesn't have an effect stated to be given
        if (effectConfig == null) {
            return;
        }

        String[] separated = effectConfig.split(":");

        // Check if fisherman is null
        if (this.fisherman == null) {
            return;
        }
        // Check if the requested player is null
        Player player = Bukkit.getPlayer(this.fisherman);
        if (player == null) {
            return;
        }

        Runnable fallback = () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
            EvenMoreFish.getInstance().getLogger().warning("Invalid potion effect specified. Defaulting to Speed 2 for 5 seconds.");
        };

        // if it's formatted wrong, it'll just give the player this as a stock effect
        if (separated.length < 3) {
            fallback.run();
            return;
        }

        PotionEffectType effect = PotionEffectType.getByName(separated[0].toUpperCase());
        // Handle the effect type being null.
        if (effect == null) {
            fallback.run();
            return;
        }
        int amplitude = Integer.parseInt(separated[1]);
        // *20 to bring it to seconds rather than ticks
        int time = Integer.parseInt(separated[2]) * 20;

        try {
            player.addPotionEffect(new PotionEffect(effect, time, amplitude));
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
        List<String> loreOverride = section.getStringList("lore-override");
        Message newLoreLine;
        if (!loreOverride.isEmpty()) {
            newLoreLine = new Message(loreOverride);
        } else {
            newLoreLine = new Message(ConfigMessage.FISH_LORE);
        }
        newLoreLine.setRarityColour(rarity.getColour());

        newLoreLine.addLore(
                "{fish_lore}",
                section.getStringList("lore")
        );

        newLoreLine.setVariable("{fisherman_lore}",
                !disableFisherman && getFishermanPlayer() != null ?
                        (new Message(ConfigMessage.FISHERMAN_LORE)).getRawMessage()
                        : ""
        );

        if (!disableFisherman && getFishermanPlayer() != null) newLoreLine.setPlayer(getFishermanPlayer());

        newLoreLine.setVariable("{length_lore}",
                length > 0 ?
                        (new Message(ConfigMessage.LENGTH_LORE)).getRawMessage()
                        : ""
        );

        if (length > 0) newLoreLine.setLength(Float.toString(length));

        newLoreLine.setRarity(this.rarity.getLorePrep());

        List<String> newLore = Arrays.asList(newLoreLine.getRawMessage().split("\n"));
        if (getFishermanPlayer() != null && EvenMoreFish.getInstance().isUsingPAPI()) {
            return newLore.stream().map(l -> PlaceholderAPI.setPlaceholders(getFishermanPlayer(), l)).collect(Collectors.toList());
        }

        return newLore;
    }

    public void checkDisplayName() {
        this.displayName = section.getString("displayname");
    }

    public void checkEatEvent() {
        List<String> configRewards = section.getStringList("eat-event");
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
        List<String> configRewards = section.getStringList("catch-event");
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
        List<String> configRewards = section.getStringList("sell-event");
        if (!configRewards.isEmpty())  {
            configRewards.forEach(reward -> {
                reward = parseEventPlaceholders(reward);
                this.sellRewards.add(new Reward(reward));
            });
        }
    }

    public void checkIntEvent() {
        List<String> configRewards = section.getStringList("interact-event");
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
        this.silent = section.getBoolean("silent", false);
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
            return rarity.getColour() + name;
        }
        return displayName;
    }

    public ItemFactory getFactory() {
        return factory;
    }

    public Requirement getRequirement() {
        return this.requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
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