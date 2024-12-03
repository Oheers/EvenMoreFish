package com.oheers.fish;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class Economy {

    private static EconomyType economyType = EconomyType.NONE;
    private boolean enabled;
    private net.milkbowl.vault.economy.Economy vaultEconomy = null;
    private PlayerPointsAPI playerPointsEconomy = null;
    private GriefPrevention griefPreventionEconomy = null;

    // TODO rework to support multiple economy types
    public Economy() {
        // TODO this should not be here, temporary added to prevent compile issues.
        EconomyType type = EconomyType.VAULT;
        enabled = false;
        EvenMoreFish emf = EvenMoreFish.getInstance();
        switch (type) {
            case VAULT -> {
                emf.getLogger().log(Level.INFO, "Attempting to hook into Vault for Economy Handling.");
                if (EvenMoreFish.getInstance().isUsingVault()) {
                    RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = emf.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    if (rsp == null) {
                        return;
                    }
                    vaultEconomy = rsp.getProvider();
                    economyType = type;
                    enabled = true;
                    emf.getLogger().log(Level.INFO, "Hooked into Vault for Economy Handling.");
                }
            }
            case PLAYER_POINTS -> {
                emf.getLogger().log(Level.INFO, "Attempting to hook into PlayerPoints for Economy Handling.");
                if (EvenMoreFish.getInstance().isUsingPlayerPoints()) {
                    this.playerPointsEconomy = PlayerPoints.getInstance().getAPI();
                    economyType = type;
                    enabled = true;
                    emf.getLogger().log(Level.INFO, "Hooked into PlayerPoints for Economy Handling.");
                }
            }
            case GRIEF_PREVENTION -> {
                emf.getLogger().log(Level.INFO, "Attempting to hook into GriefPrevention for Economy Handling.");
                if (EvenMoreFish.getInstance().isUsingGriefPrevention()) {
                    this.griefPreventionEconomy = GriefPrevention.instance;
                    economyType = type;
                    enabled = true;
                    emf.getLogger().info("Hooked into GriefPrevention for Economy Handling.");
                }
            }
        }
    }

    public EconomyType getEconomyType() { return economyType; }

    public boolean isEnabled() { return enabled; }

    public void deposit(@NotNull OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT -> vaultEconomy.depositPlayer(player, amount);
            // PlayerPoints doesn't support doubles, so we need to cast to int
            case PLAYER_POINTS -> playerPointsEconomy.give(player.getUniqueId(), (int) amount);
            case GRIEF_PREVENTION -> {
                PlayerData data = griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId());
                data.setBonusClaimBlocks(data.getBonusClaimBlocks() + (int) amount);
            }
        }
    }

    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        return switch (economyType) {
            case VAULT -> vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            // PlayerPoints doesn't support doubles, so we need to cast to int
            case PLAYER_POINTS -> playerPointsEconomy.take(player.getUniqueId(), (int) amount);
            case GRIEF_PREVENTION -> {
                PlayerData data = griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId());
                int total = data.getBonusClaimBlocks();
                int finalTotal = total - (int) amount;
                if (finalTotal < 0) {
                    yield false;
                }
                data.setBonusClaimBlocks(finalTotal);
                yield true;
            }
            default -> true;
        };
    }

    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return switch (economyType) {
            case VAULT -> vaultEconomy.has(player, amount);
            case PLAYER_POINTS ->
                // PlayerPoints doesn't seem to have a method to check this
                    playerPointsEconomy.look(player.getUniqueId()) >= amount;
            case GRIEF_PREVENTION -> griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId()).getBonusClaimBlocks() >= amount;
            default -> true;
        };
    }

    public double get(@NotNull OfflinePlayer player) {
        return switch (economyType) {
            case VAULT -> vaultEconomy.getBalance(player);
            case PLAYER_POINTS ->
                // PlayerPoints doesn't seem to have a method to check this
                    playerPointsEconomy.look(player.getUniqueId());
            case GRIEF_PREVENTION -> griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId()).getBonusClaimBlocks();
            default -> 0;
        };
    }

    public static double prepareValue(double value) {
        return switch (economyType) {
            case VAULT -> value;
            case PLAYER_POINTS, GRIEF_PREVENTION -> Math.floor(value);
            default -> 0;
        };
    }

    public enum EconomyType {
        PLAYER_POINTS,
        VAULT,
        GRIEF_PREVENTION,
        NONE
    }

}
