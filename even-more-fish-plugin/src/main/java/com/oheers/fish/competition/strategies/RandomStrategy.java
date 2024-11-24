package com.oheers.fish.competition.strategies;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.CompetitionType;

public class RandomStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        competition.competitionType = getRandomType();
        Competition.setOriginallyRandom(true);
        return true;
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

    public CompetitionType getRandomType() {
        // -1 from the length so that the RANDOM isn't chosen as the random value.
        int type = EvenMoreFish.getInstance().getRandom().nextInt(CompetitionType.values().length - 1);
        return CompetitionType.values()[type];
    }
}
