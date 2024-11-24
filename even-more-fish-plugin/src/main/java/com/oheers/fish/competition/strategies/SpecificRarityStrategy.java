package com.oheers.fish.competition.strategies;



import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public Message getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        final Message message = CompetitionStrategy.super.getTypeFormat(competition, configMessage);
        message.setAmount(Integer.toString(competition.numberNeeded));
        if (competition.selectedRarity == null) {
            EvenMoreFish.getInstance().getLogger().warning("Null rarity found. Please check your config files.");
            return message;
        }
        message.setRarityColour(competition.selectedRarity.getColour());
        message.setRarity(competition.selectedRarity.getDisplayName());

        return message;
    }
}
