package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoonPhaseRequirementType implements RequirementType {

    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values) {
        World world = context.getWorld();
        if (world == null) {
            return false;
        }
        int phaseId = (int) (world.getFullTime() / 24000) % 8;
        for (String value : values) {
            @NotNull Phase phase;
            try {
                phase = Phase.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException exception) {
                EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid moon phase.");
                return false;
            }
            if (phase.getPhaseID() == phaseId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MOON-PHASE";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
