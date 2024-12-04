package com.oheers.fish;

import com.oheers.fish.economy.EconomyType;
import com.oheers.fish.economy.types.VaultEconomyType;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Economy {

    private List<EconomyType> registeredEconomies;

    public Economy() {
        registeredEconomies = new ArrayList<>(List.of(
                new VaultEconomyType()
        ));
    }

    public List<EconomyType> getRegisteredEconomies() { return List.copyOf(registeredEconomies); }

    /**
     * @return True if any registered economy is available.
     */
    public boolean isEnabled() {
        for (EconomyType economyType : registeredEconomies) {
            if (economyType.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void deposit(@NotNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        registeredEconomies.forEach(type -> type.deposit(player, amount, applyMultiplier));
    }

    public void withdraw(@NotNull OfflinePlayer player, double amount, boolean applyMultiplier) {
        registeredEconomies.forEach(type -> type.withdraw(player, amount, applyMultiplier));
    }

    public Map<EconomyType, Double> get(@NotNull OfflinePlayer player) {
        Map<EconomyType, Double> valuesMap = new HashMap<>();
        registeredEconomies.forEach(type -> valuesMap.put(type, type.get(player)));
        return valuesMap;
    }

    public @Nullable EconomyType getEconomyType(@NotNull String identifier) {
        for (EconomyType type : registeredEconomies) {
            if (type.isAvailable() && type.getIdentifier().equalsIgnoreCase(identifier)) {
                return type;
            }
        }
        return null;
    }

    public @NotNull String getWorthFormat(double value, boolean applyMultiplier) {
        return registeredEconomies.stream()
                .map(type -> type.formatWorth(value, applyMultiplier))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

}
