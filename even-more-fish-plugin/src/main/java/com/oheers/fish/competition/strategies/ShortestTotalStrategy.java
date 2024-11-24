package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;

public class ShortestTotalStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        return true;
    }

    @Override
    public void applyLeaderboard() {

    }

    @Override
    public Message getSingleConsoleLeaderboardMessage(Message message, CompetitionEntry entry) {
        message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_TOTAL);
        message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
        return message;
    }

    @Override
    public Message getSinglePlayerLeaderboard(Message message, CompetitionEntry entry) {
        message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_TOTAL);
        message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
        return message;
    }
}
