package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MoonPhase implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;

    public final List<Phase> phases = new ArrayList<>();

    @Override
    public boolean requirementMet(RequirementContext context) {
        int phaseID = (int) (context.getWorld().getFullTime()/24000)%8;
        for (Phase phase : phases) {
            if (phase.getPhaseID() == phaseID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void fetchData() {
        fileConfig.getStringList(this.configLocation).forEach(stringPhase -> {
            try {
                phases.add(Phase.valueOf(stringPhase.toUpperCase()));
            } catch (IllegalArgumentException exception) {
                EvenMoreFish.getInstance().getLogger().severe(stringPhase + " is not a valid moon phase at fish.yml location " + this.configLocation);
            }
        });
    }

    /**
     * This checks the current world time and converts it into a "moon phase" which represents the one that can be seen
     * in the night sky. It is advisable to use this in conjunction with the InGameTime requirement as the moon and
     * subsequently its phase is only visible during night.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "moon-phase:"
     *                       for example, "fish.Common.Herring.requirements.moon-phase".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public MoonPhase(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }
}

