package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.config.BaitFile;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.utils.ItemFactory;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Bait {

    private final Section section;
    private final ItemFactory itemFactory;
    private final String name, displayName, theme;
    private final int maxApplications, dropQuantity;
    List<Fish> fishList = new ArrayList<>();
    List<Rarity> rarityList = new ArrayList<>();
    Set<Rarity> fishListRarities = new HashSet<>();
    double boostRate;
    double applicationWeight;
    double catchWeight;

    /**
     * This represents a bait, which can be used to boost the likelihood that a certain fish or fish rarity appears from
     * the rod. All data is fetched from the config when the Bait object is created and then can be given out using
     * the create() method.
     * <p>
     * The plugin recognises the bait item from the create() method using NBT data, which can be applied using the
     * BaitNBTManager class, which handles all the NBT thingies.
     *
     * @param section The config section of the bait from the baits.yml file
     */
    public Bait(@NotNull Section section) {
        this.section = section;
        this.name = Objects.requireNonNull(section.getNameAsString());

        this.theme = FishUtils.translateColorCodes(section.getString("bait-theme", "&e"));

        this.applicationWeight = section.getDouble("application-weight");
        this.catchWeight = section.getDouble("catch-weight");

        this.boostRate = BaitFile.getInstance().getBoostRate();
        this.maxApplications = section.getInt("max-baits", -1);
        this.displayName = section.getString("item.displayname");
        this.dropQuantity = section.getInt("drop-quantity", 1);

        loadRarities();
        loadFish();

        ItemFactory factory = new ItemFactory(null, section);
        factory.enableDefaultChecks();
        factory.setItemDisplayNameCheck(true);
        factory.setDisplayName(FishUtils.translateColorCodes("&e" + name));
        this.itemFactory = factory;
    }

    /**
     * This creates an item based on random settings in the yml files, adding things such as custom model data and glowing
     * effects.
     *
     * @return An item stack representing the bait object, with nbt.
     */
    public ItemStack create(OfflinePlayer player) {
        ItemStack baitItem = itemFactory.createItem(player, -1);
        baitItem.setAmount(dropQuantity);

        ItemMeta meta = baitItem.getItemMeta();
        if (meta != null) {
            meta.setLore(createBoostLore());
        }
        baitItem.setItemMeta(meta);

        return BaitNBTManager.applyBaitNBT(baitItem, this.name);
    }

    /**
     * This adds a single fish to the list of fish this bait will affect. If adding a whole rarity, use the addRarity()
     * method instead.
     *
     * @param f The fish being boosted.
     */
    public void addFish(Fish f) {
        fishListRarities.add(f.getRarity());
        fishList.add(f);
    }

    /**
     * @return All configured rarities from this bait's configuration.
     */
    public @NotNull List<Rarity> getRarities() {
        List<String> rarityStrings = section.getStringList("rarities");
        return rarityStrings.stream()
                .map(FishManager.getInstance()::getRarity)
                .filter(Objects::nonNull)
                .toList();
    }

    private void loadRarities() {
        for (String rarityName : section.getStringList("rarities")) {
            Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                continue;
            }
            rarityList.add(rarity);
        }
    }

    private void loadFish() {
        Section fishSection = section.getSection("fish");
        if (fishSection == null) {
            return;
        }
        for (String rarityName : fishSection.getRoutesAsStrings(false)) {
            Rarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
                continue;
            }
            List<String> fishNames = fishSection.getStringList(rarityName);
            for (String fishName : fishNames) {
                Fish fish = rarity.getFish(fishName);
                if (fish == null) {
                    continue;
                }
                fishList.add(fish);
            }
        }
    }

    public @NotNull List<Fish> getFish() {
        return fishList;
    }

    /**
     * This fetches the boost's lore from the config and inserts the boost-rates into the {boosts} variable. This needs
     * to be called after the bait theme is set and the boosts have been initialized, since it uses those variables.
     */
    private List<String> createBoostLore() {

        List<String> lore = new ArrayList<>();

        for (String lineAddition : BaitFile.getInstance().getBaitLoreFormat()) {
            if (lineAddition.equals("{boosts}")) {

                if (!rarityList.isEmpty()) {
                    AbstractMessage message;
                    if (rarityList.size() > 1) {
                        message = EvenMoreFish.getAdapter().createMessage(BaitFile.getInstance().getBoostRaritiesFormat());
                    } else {
                        message = EvenMoreFish.getAdapter().createMessage(BaitFile.getInstance().getBoostRarityFormat());
                    }
                    message.setAmount(Integer.toString(rarityList.size()));
                    message.setBaitTheme(theme);
                    lore.add(message.getLegacyMessage());
                }

                if (!fishList.isEmpty()) {
                    AbstractMessage message = EvenMoreFish.getAdapter().createMessage(BaitFile.getInstance().getBoostFishFormat());
                    message.setAmount(Integer.toString(fishList.size()));
                    message.setBaitTheme(theme);
                    lore.add(message.getLegacyMessage());
                }

            } else if (lineAddition.equals("{lore}")) {
                section.getStringList("lore").forEach(line -> {
                    AbstractMessage message = EvenMoreFish.getAdapter().createMessage(line);
                    lore.add(message.getLegacyMessage());
                });
            } else {
                AbstractMessage message = EvenMoreFish.getAdapter().createMessage(lineAddition);
                message.setBaitTheme(theme);
                lore.add(message.getLegacyMessage());
            }
        }

        return lore;
    }

    public boolean isInfinite() {
        return section.getBoolean("infinite", false);
    }

    public boolean shouldDisableUseAlert() {
        return section.getBoolean("disable-use-alert", false);
    }

    /**
     * This chooses a random fish based on the set boosts of the bait's config.
     * <p>
     * If there's rarities in the rarityList, choose a rarity first, applying multiplication of weight.
     * If there's no rarities in the server list: *
     * Check if there's any fish in the bait for this rarity, boost them. REMOVE BAIT
     * If the rarity chosen was not boosted, check if any fish are in this rarity and boost them. REMOVE BAIT
     * <p>
     * * Pick a rarity, boosting all rarities referenced in the fishList, from that rarity choose a random fish, if that
     * fish is within the fishList then give it to the player as the fish roll. REMOVE BAIT
     *
     * @return A chosen fish.
     */
    public Fish chooseFish(Player player, Location location) {
        Set<Rarity> boostedRarities = new HashSet<>(getRarities());
        boostedRarities.addAll(fishListRarities);

        Rarity fishRarity = FishManager.getInstance().getRandomWeightedRarity(player, getBoostRate(), boostedRarities, Set.copyOf(FishManager.getInstance().getRarityMap().values()));
        Fish fish;

        if (!getFish().isEmpty()) {
            // The bait has both rarities: and fish: set but the plugin chose a rarity with no boosted fish. This ensures
            // the method isn't given an empty list.
            if (!fishListRarities.contains(fishRarity)) {
                fish = FishManager.getInstance().getFish(fishRarity, location, player, BaitFile.getInstance().getBoostRate(), fishRarity.getFishList(), true);
            } else {
                fish = FishManager.getInstance().getFish(fishRarity, location, player, BaitFile.getInstance().getBoostRate(), getFish(), true);
            }
            if (fish != null) {
                fish.setWasBaited(true);
            }

            if (!getRarities().contains(fishRarity) && (fish == null || !getFish().contains(fish))) {
                // boost effect chose a fish but the randomizer didn't pick out the right fish - they've been incorrectly boosted.
                fish = FishManager.getInstance().getFish(fishRarity, location, player, 1, null, true);
                if (fish != null) {
                    fish.setWasBaited(false);
                }
            } else {
                alertUsage(player);
            }
        } else {
            fish = FishManager.getInstance().getFish(fishRarity, location, player, 1, null, true);
            if (getRarities().contains(fishRarity)) {
                alertUsage(player);
                if (fish != null) {
                    fish.setWasBaited(true);
                }
            }
        }

        return fish;
    }

    /**
     * Lets the player know that they've used one of their baits. Uses the value in messages.yml under "bait-use".
     *
     * @param player The player that's used the bait.
     */

    private void alertUsage(Player player) {
        if (shouldDisableUseAlert()) {
            return;
        }

        AbstractMessage message = ConfigMessage.BAIT_USED.getMessage();
        message.setBait(this.name);
        message.setBaitTheme(this.theme);
        message.send(player);

    }

    /**
     * @return How likely the bait is to apply out of all others applied baits.
     */
    public double getApplicationWeight() {
        return applicationWeight;
    }

    /**
     * @param applicationWeight How likely the bait is to apply out of all others applied baits.
     */
    public void setApplicationWeight(double applicationWeight) {
        this.applicationWeight = applicationWeight;
    }

    /**
     * @return How likely the bait is to appear out of all other baits when caught.
     */
    public double getCatchWeight() {
        return catchWeight;
    }

    /**
     * @param catchWeight How likely the bait is to appear out of all other baits when caught.
     */
    public void setCatchWeight(double catchWeight) {
        this.catchWeight = catchWeight;
    }

    /**
     * @return The x multiplier of a chance to get one of the fish in the bait's fish to appear.
     */
    public double getBoostRate() {
        return boostRate;
    }

    /**
     * @param boostRate The x multiplier of a chance to get one of the fish in the bait's fish to appear.
     */
    public void setBoostRate(double boostRate) {
        this.boostRate = boostRate;
    }

    /**
     * @return The name identifier of the bait.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The colour theme defined for the bait.
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @return How many of this bait can be applied to a fishing rod.
     */
    public int getMaxApplications() {
        return maxApplications;
    }

    /**
     * @return The displayname setting for the bait.
     */
    public String getDisplayName() {
        if (displayName == null) {
            return name;
        }
        return displayName;
    }
}
