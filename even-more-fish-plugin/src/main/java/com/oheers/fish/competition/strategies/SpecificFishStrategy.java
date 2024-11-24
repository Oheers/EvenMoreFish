package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public Message getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        Message message = CompetitionStrategy.super.getTypeFormat(competition, configMessage);
        message.setAmount(Integer.toString(competition.numberNeeded));
        message.setRarityColour(competition.selectedFish.getRarity().getColour());
        message.setRarity(competition.selectedFish.getRarity().getDisplayName());
        message.setFishCaught(competition.selectedFish.getDisplayName());
        return message;
    }
}
