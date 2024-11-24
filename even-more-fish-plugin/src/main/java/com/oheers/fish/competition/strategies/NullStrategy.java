package com.oheers.fish.competition.strategies;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionStrategy;


public class NullStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        return true;
    }

    @Override
    public void applyLeaderboard() {
        //nothing
    }

    @Override
    public void applyConsoleLeaderboard() {
        //nothing
    }

    @Override
    public void sendPlayerLeaderboard() {
        //nothing
    }
}
