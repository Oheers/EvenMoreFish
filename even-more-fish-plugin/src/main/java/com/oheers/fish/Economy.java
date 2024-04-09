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
    private boolean enabled = false;
    private net.milkbowl.vault.economy.Economy vaultEconomy = null;
    private PlayerPointsAPI playerPointsEconomy = null;
    private GriefPrevention griefPreventionEconomy = null;

    public Economy(EconomyType type) {
        EvenMoreFish emf = EvenMoreFish.getInstance();
        switch (type) {
            case VAULT:
                emf.getLogger().log(Level.INFO, "Attempting to hook into Vault for Economy Handling.");
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = emf.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (rsp == null) {
                    return;
                }
                vaultEconomy = rsp.getProvider();
                economyType = type;
                enabled = true;
                emf.getLogger().log(Level.INFO, "Hooked into Vault for Economy Handling.");
                return;
            case PLAYER_POINTS:
                emf.getLogger().log(Level.INFO, "Attempting to hook into PlayerPoints for Economy Handling.");
                if (EvenMoreFish.getInstance().isUsingPlayerPoints()) {
                    this.playerPointsEconomy = PlayerPoints.getInstance().getAPI();
                    economyType = type;
                    enabled = true;
                    emf.getLogger().log(Level.INFO, "Hooked into PlayerPoints for Economy Handling.");
                }
                return;
            case GRIEF_PREVENTION:
                emf.getLogger().log(Level.INFO, "Attempting to hook into GriefPrevention for Economy Handling.");
                if (EvenMoreFish.getInstance().isUsingGriefPrevention()) {
                    this.griefPreventionEconomy = GriefPrevention.instance;
                    economyType = type;
                    enabled = true;
                    emf.getLogger().info("Hooked into GriefPrevention for Economy Handling.");
                }
        }
    }

    public EconomyType getEconomyType() { return economyType; }

    public boolean isEnabled() { return enabled; }

    public void deposit(@NotNull OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                vaultEconomy.depositPlayer(player, amount);
                return;
            case PLAYER_POINTS:
                // PlayerPoints doesn't support doubles, so we need to cast to int
                playerPointsEconomy.give(player.getUniqueId(), (int) amount);
                return;
            case GRIEF_PREVENTION:
                PlayerData data = griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId());
                data.setBonusClaimBlocks(data.getBonusClaimBlocks() + (int) amount);
        }
    }

    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            case PLAYER_POINTS:
                // PlayerPoints doesn't support doubles, so we need to cast to int
                return playerPointsEconomy.take(player.getUniqueId(), (int) amount);
            case GRIEF_PREVENTION:
                PlayerData data = griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId());
                int total = data.getBonusClaimBlocks();
                int finalTotal = total - (int) amount;
                if (finalTotal < 0) {
                    return false;
                }
                data.setBonusClaimBlocks(finalTotal);
                return true;
            default:
                return true;
        }
    }

    public boolean has(@NotNull OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                return vaultEconomy.has(player, amount);
            case PLAYER_POINTS:
                // PlayerPoints doesn't seem to have a method to check this
                return playerPointsEconomy.look(player.getUniqueId()) >= amount;
            case GRIEF_PREVENTION:
                return griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId()).getBonusClaimBlocks() >= amount;
            default:
                return true;
        }
    }

    public double get(@NotNull OfflinePlayer player) {
        switch (economyType) {
            case VAULT:
                return vaultEconomy.getBalance(player);
            case PLAYER_POINTS:
                // PlayerPoints doesn't seem to have a method to check this
                return playerPointsEconomy.look(player.getUniqueId());
            case GRIEF_PREVENTION:
                return griefPreventionEconomy.dataStore.getPlayerData(player.getUniqueId()).getBonusClaimBlocks();
            default:
                return 0;
        }
    }

    public static double prepareValue(double value) {
        switch (economyType) {
            case VAULT:
                return value;
            case PLAYER_POINTS:
            case GRIEF_PREVENTION:
                return Math.floor(value);
            default:
                return 0;
        }
    }

    public enum EconomyType {
        PLAYER_POINTS,
        VAULT,
        GRIEF_PREVENTION,
        NONE
    }

}
