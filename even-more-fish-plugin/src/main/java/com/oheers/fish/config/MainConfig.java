package com.oheers.fish.config;

import com.oheers.fish.Economy;
import com.oheers.fish.EvenMoreFish;
import org.bukkit.Material;

import java.util.List;

public class MainConfig extends ConfigBase {

    private static MainConfig instance = null;

    public MainConfig() {
        super("config.yml", EvenMoreFish.getInstance());
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

    public String getFiller() {
        return getConfig().getString("gui.filler", "GRAY_STAINED_GLASS_PANE");
    }

    public String getFillerError() {
        return getConfig().getString("gui.filler-error", "RED_STAINED_GLASS_PANE");
    }

    public String getSellItem() {
        return getConfig().getString("gui.sell-item", "GOLD_INGOT");
    }

    public String getSellItemConfirm() {
        return getConfig().getString("gui.sell-item-confirm", "GOLD_BLOCK");
    }

    public String getSellItemError() {
        return getConfig().getString("gui.sell-item-error", "REDSTONE_BLOCK");
    }

    public Integer getGUISize() {
        int returning = getConfig().getInt("gui.size", 3);
        if (returning <= 0 || returning > 5) return 3;
        else return returning;
    }

    public String getBarStyle() {
        return getConfig().getString("barstyle", "SEGMENTED_10");
    }

    public boolean sellOverDrop() {
        return getConfig().getBoolean("gui.sell-over-drop", false);
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

    public Material getSellAllMaterial() {
        String s = getConfig().getString("gui.sell-all-item", "COD_BUCKET");
        try {
            return Material.valueOf(s);
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().severe(s + " is not a valid material type in config.yml gui.sell-all-item.");
        }
        return Material.COD_BUCKET;
    }

    public Material getSellAllConfirmMaterial() {
        String s = getConfig().getString("gui.sell-all-item-confirm", "TROPICAL_FISH_BUCKET");
        try {
            return Material.valueOf(s);
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().severe(s + " is not a valid material type in config.yml gui.sell-all-item-confirm.");
        }
        return Material.TROPICAL_FISH_BUCKET;
    }

    public Material getSellAllErrorMaterial() {
        String s = getConfig().getString("gui.sell-all-item-error", "SALMON_BUCKET");
        try {
            return Material.valueOf(s);
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance().getLogger().severe(s + " is not a valid material type in config.yml gui.sell-all-item-confirm.");
        }
        return Material.SALMON_BUCKET;
    }

    public int getSellAllSlot() {
        int returning = getConfig().getInt("gui.sell-all-slot", 6);
        if (returning > 9 || returning < 1) return 6;
        else return returning;
    }

    public int getSellSlot() {
        int returning = getConfig().getInt("gui.sell-slot", 4);
        if (returning > 9 || returning < 1) return 4;
        else return returning;
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

}
