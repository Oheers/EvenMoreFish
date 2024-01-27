package com.oheers.fish;

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

    public Economy(EconomyType type) {
        EvenMoreFish emf = EvenMoreFish.getInstance();
        System.out.println(type.toString());
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
                if (EvenMoreFish.usingPlayerPoints) {
                    this.playerPointsEconomy = PlayerPoints.getInstance().getAPI();
                    economyType = type;
                    enabled = true;
                    emf.getLogger().log(Level.INFO, "Hooked into PlayerPoints for Economy Handling.");
                }
                return;
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
        }
    }

    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        switch (economyType) {
            case VAULT:
                return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            case PLAYER_POINTS:
                // PlayerPoints doesn't support doubles, so we need to cast to int
                return playerPointsEconomy.take(player.getUniqueId(), (int) amount);
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
            default:
                return 0;
        }
    }

    public static double prepareValue(double value) {
        switch (economyType) {
            case VAULT:
                return value;
            case PLAYER_POINTS:
                return Math.floor(value);
            default:
                return 0;
        }
    }

    public enum EconomyType {
        PLAYER_POINTS,
        VAULT,
        NONE
    }

}
