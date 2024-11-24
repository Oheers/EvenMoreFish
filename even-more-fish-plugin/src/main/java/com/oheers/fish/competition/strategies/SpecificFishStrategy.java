package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionStrategy;

public class SpecificFishStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        return competition.chooseFish();
    }

    @Override
    public void applyLeaderboard() {

    }

    @Override
    public void applyConsoleLeaderboard() {

    }

    @Override
    public void sendPlayerLeaderboard() {

    }
}
