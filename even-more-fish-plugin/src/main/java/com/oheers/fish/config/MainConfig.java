package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.EconomyType;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.apache.commons.lang3.LocaleUtils;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MainConfig extends ConfigBase {

    private static MainConfig instance = null;

    public MainConfig() {
        super("config.yml", "config.yml", EvenMoreFish.getInstance(), true);
        instance = this;
        applyOneTimeConversions();
    }

    public static MainConfig getInstance() {
        return instance;
    }

    public String getLocale() {
        return getConfig().getString("locale", "en");
    }

    public int getCompetitionDuration() {
        return getConfig().getInt("competitions.duration");
    }

    public boolean doingRandomDurability() {
        return getConfig().getBoolean("random-durability", true);
    }



    public boolean isDatabaseOnline() {
        return databaseEnabled() && !EvenMoreFish.getInstance().getDatabaseV3().usingVersionV2();
    }

    public boolean isCompetitionUnique() {
        return getConfig().getBoolean("fish-only-in-competition", false);
    }

    public boolean getEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    public boolean worldWhitelist() {
        return !getConfig().getStringList("allowed-worlds").isEmpty();
    }

    public List<String> getAllowedRegions() {
        return getConfig().getStringList("allowed-regions");
    }

    public List<String> getAllowedWorlds() {
        return getConfig().getStringList("allowed-worlds");
    }

    public boolean shouldRespectVanish() { return getConfig().getBoolean("respect-vanished", true); }

    public boolean shouldProtectBaitedRods() { return getConfig().getBoolean("protect-baited-rods", true); }
    
    public boolean isVanillaFishing() {
        return getConfig().getBoolean("vanilla-fishing", true);
    }

    public BarStyle getBarStyle() {
        BarStyle barStyle;
        try {
            barStyle = BarStyle.valueOf(getConfig().getString("barstyle", "SEGMENTED_10"));
        } catch (IllegalArgumentException exception) {
            barStyle = BarStyle.SEGMENTED_10;
        }
        return barStyle;
    }

    public boolean sellOverDrop() {
        return getConfig().getBoolean("sell-gui.sell-over-drop", false);
    }

    public boolean disableMcMMOTreasure() {
        return getConfig().getBoolean("disable-mcmmo-loot", true);
    }

    public boolean disableAureliumSkills() {
        return getConfig().getBoolean("disable-aureliumskills-loot", true);
    }

    public String rewardEffect() {
        return getConfig().getString("reward-gui.reward-effect");
    }

    public String rewardItem() {
        return getConfig().getString("reward-gui.reward-item");
    }

    public String rewardMoney() {
        return getConfig().getString("reward-gui.reward-money");
    }

    public String rewardHealth() {
        return getConfig().getString("reward-gui.reward-health");
    }

    public String rewardHunger() {
        return getConfig().getString("reward-gui.reward-hunger");
    }

    public String rewardCommand(String command) {
        return getConfig().getString("reward-gui.command-override." + command);
    }


    public boolean doDBVerbose() {
        return !getConfig().getBoolean("disable-db-verbose", false);
    }



    public boolean blockPlacingHeads() {
        return getConfig().getBoolean("place-head-fish", false);
    }

    public boolean requireNBTRod() {
        return getConfig().getBoolean("require-nbt-rod", false);
    }

    public boolean requireFishingPermission() {
        return getConfig().getBoolean("requires-fishing-permission", false);
    }

    public boolean blockCrafting() {
        return getConfig().getBoolean("block-crafting", false);
    }
    public boolean debugSession() {
        return getConfig().getBoolean("debug-session", false);
    }

    public boolean databaseEnabled() {
        return getConfig().getBoolean("database.enabled", false);
    }

    public String getAddress() {
        return getConfig().getString("database.address", "localhost");
    }

    public String getDatabase() {
        return getConfig().getString("database.database", "database");
    }

    public String getUsername() {
        return getConfig().getString("database.username", "root");
    }

    public String getPassword() {
        return getConfig().getString("database.password", "");
    }

    public String getPrefix() {
        return getConfig().getString("database.prefix", "emf_");
    }

    public String getDatabaseType() {
        return getConfig().getString("database.type", "sqlite");
    }


    public boolean useAdditionalAddons() {
        return getConfig().getBoolean("addons.additional-addons", true);
    }

    public Locale getDecimalLocale() {
        final String locale = getConfig().getString(Route.fromString("decimal-locale"), "en-US");
        return LocaleUtils.toLocale(locale);
    }


    public int getNearbyPlayersRequirementRange() { return getConfig().getInt("requirements.nearby-players.range", 0); }

    public String getMainCommandName() {
        return getConfig().getString("command.main", "emf");
    }

    public List<String> getMainCommandAliases() {
        return getConfig().getStringList("command.aliases");
    }
  
    public String[] getSellGUILayout() {
        List<String> layout = getConfig().getStringList("gui.layout");

        // Return default layout if the config is empty
        if (layout.isEmpty()) {
            return new String[]{
                    "iiiiiiiii",
                    "iiiiiiiii",
                    "iiiiiiiii",
                    "fffsfafff"
            };
        }

        // Convert the List<String> to a String[] and return
        return layout.toArray(new String[0]);
    }

    public boolean giveStraightToInventory() {
        return getConfig().getBoolean("give-straight-to-inventory");
    }

    public Map<String, List<Biome>> getBiomeSets() {
        Map<String, List<Biome>> biomeSetMap = new HashMap<>();
        Section section = getConfig().getSection("biome-sets");
        if (section == null) {
            return Map.of();
        }
        section.getRoutesAsStrings(false).forEach(key -> {
            List<Biome> biomes = new ArrayList<>();
            section.getStringList(key).forEach(biomeString -> {
                Biome biome = FishUtils.getBiome(biomeString);
                if (biome == null) {
                    EvenMoreFish.getInstance().getLogger().severe(biomeString + " is not a valid biome, found when loading in biome set " + key + ".");
                }
                biomes.add(biome);
            });
            biomeSetMap.put(key, biomes);
        });
        return biomeSetMap;
    }

    public double getRegionBoost(String region, String rarity) {
        if (region == null || rarity == null) {
            return 1.0; // Default boost rate is 1.0 if region or rarity is null
        }

        Section regionBoosts = getConfig().getSection("region-boosts");
        if (regionBoosts == null) {
            return 1.0; // Default boost rate is 1.0 if not specified
        }

        Section regionSection = regionBoosts.getSection(region);
        if (regionSection == null) {
            return 1.0; // Default boost rate is 1.0 if not specified
        }

        return regionSection.getDouble(rarity, 1.0); // Default boost rate is 1.0 if not specified
    }

    public boolean isRegionBoostsEnabled() {
        return getConfig().contains("region-boosts") && getConfig().isSection("region-boosts");
    }

    public boolean isEconomyEnabled(@NotNull EconomyType type) {
        return getConfig().getBoolean("economy." + type.getIdentifier().toLowerCase() + ".enabled");
    }

    public double getEconomyMultiplier(@NotNull EconomyType type) {
        return getConfig().getDouble("economy." + type.getIdentifier().toLowerCase() + ".multiplier");
    }

    public @Nullable String getEconomyDisplay(@NotNull EconomyType type) {
        return getConfig().getString("economy." + type.getIdentifier().toLowerCase() + ".display");
    }

    private void applyOneTimeConversions() {
        YamlDocument yamlDocument = getConfig();

        // Economy Rework - Requires the config to contain the new format first.
        String economyType = yamlDocument.getString("economy-type");
        if (economyType != null) {
            yamlDocument.remove("enable-economy");
            yamlDocument.remove("economy-type");
            if (!economyType.equalsIgnoreCase("NONE")) {
                String path = "economy." + economyType.toLowerCase();
                yamlDocument.set(path + ".enabled", true);
            }
        }

        save();
    }

}
