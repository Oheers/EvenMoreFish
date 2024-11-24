package com.oheers.fish.competition.strategies;



import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionStrategy;

public class SpecificRarityStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        return competition.chooseRarity();
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
