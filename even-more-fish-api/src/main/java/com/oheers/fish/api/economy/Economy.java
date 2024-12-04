package com.oheers.fish.api.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Economy {

    private List<EconomyType> registeredEconomies;
    private static Economy instance = null;

    private Economy() {
        registeredEconomies = new ArrayList<>();
    }

    public static Economy getInstance() {
        if (instance == null) {
            instance = new Economy();
        }
        return instance;
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

    public boolean registerEconomyType(@NotNull EconomyType economyType) {
        if (getEconomyType(economyType.getIdentifier()) != null) {
            return false;
        }
        return registeredEconomies.add(economyType);
    }

}
