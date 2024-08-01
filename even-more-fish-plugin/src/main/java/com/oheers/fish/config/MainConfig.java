package com.oheers.fish.config;

import com.oheers.fish.Economy;
import com.oheers.fish.EvenMoreFish;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.apache.commons.lang3.LocaleUtils;
import org.bukkit.block.Biome;

import java.util.*;

public class MainConfig extends ConfigBase {

    private static MainConfig instance = null;

    public MainConfig() {
        super("config.yml", "config.yml", EvenMoreFish.getInstance(), true);
        instance = this;
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

    public boolean isEconomyEnabled() {
        return getConfig().getBoolean("enable-economy", true);
    }

    public boolean isEconomyDisabled() {
        return !isEconomyEnabled();
    }

    public Economy.EconomyType economyType() {
        String economyString = getConfig().getString("economy-type", "Vault");
        return switch (economyString) {
            case "Vault" -> Economy.EconomyType.VAULT;
            case "PlayerPoints" -> Economy.EconomyType.PLAYER_POINTS;
            case "GriefPrevention" -> Economy.EconomyType.GRIEF_PREVENTION;
            default -> Economy.EconomyType.NONE;
        };
    }

    public boolean shouldRespectVanish() { return getConfig().getBoolean("respect-vanished", true); }

    public boolean shouldProtectBaitedRods() { return getConfig().getBoolean("protect-baited-rods", true); }
    
    public boolean isVanillaFishing() {
        return getConfig().getBoolean("vanilla-fishing", true);
    }

    public String getBarStyle() {
        return getConfig().getString("barstyle", "SEGMENTED_10");
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
                try {
                    biomes.add(Biome.valueOf(biomeString));
                } catch (IllegalArgumentException exception) {
                    EvenMoreFish.getInstance().getLogger().severe(biomeString + " is not a valid biome, found when loading in biome set " + key + ".");
                }
            });
            biomeSetMap.put(key, biomes);
        });
        return biomeSetMap;
    }

}
