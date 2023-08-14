package com.oheers.fish.requirements;

public enum Phase {

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
