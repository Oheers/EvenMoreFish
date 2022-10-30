package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MoonPhase implements Requirement {

    public final String configLocation;

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
        EvenMoreFish.fishFile.getConfig().getStringList(this.configLocation).forEach((stringPhase) -> {
            try {
                phases.add(Phase.valueOf(stringPhase.toUpperCase()));
            } catch (IllegalArgumentException exception) {
                EvenMoreFish.logger.log(Level.SEVERE, stringPhase + " is not a valid moon phase at fish.yml location " + this.configLocation);
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
     */
    public MoonPhase(@NotNull final String configLocation) {
        this.configLocation = configLocation;
        fetchData();
    }
}

enum Phase {

    FULL_MOON(0),
    WANING_GIBBOUS(1),
    LAST_QUARTER(2),
    WANING_CRESCENT(3),
    NEW_MOON(4),
    WAXING_CRESCENT(5),
    FIRST_QUARTER(6),
    WAXING_GIBBOUS(7);

    final int phaseID;

    Phase(int phaseID) {
        this.phaseID = phaseID;
    }

    public int getPhaseID() {
        return phaseID;
    }
}
