package com.oheers.fish.api.requirement;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A way to check if a player meets a certain requirement.
 * This interface can be implemented by third party plugins to register their own Requirement.
 */
public interface RequirementType {

    /**
     * Checks if a player meets this requirement.
     * @param player The player to check
     * @param value The value to check
     */
    boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values);

    /**
     * The identifier for this Requirement
     * @return The identifier for this Requirement
     */
    @NotNull String getIdentifier();

    @NotNull String getAuthor();

    @NotNull Plugin getPlugin();

    default boolean register() {
        return RequirementManager.getInstance().registerRequirement(this);
    }

}
