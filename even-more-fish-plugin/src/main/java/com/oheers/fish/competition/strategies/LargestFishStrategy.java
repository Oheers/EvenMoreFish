package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

public class LargestFishStrategy implements CompetitionStrategy {

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        if (fish.getLength() <= 0) return;

        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

        if (entry != null) {
            if (fish.getLength() > entry.getFish().getLength()) {
                leaderboard.removeEntry(entry);
                leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
            }
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
        }
    }

    @Override
    public Message getSingleConsoleLeaderboardMessage(Message message, CompetitionEntry entry) {
        Fish fish = entry.getFish();
        message.setRarityColour(fish.getRarity().getColour());
        message.setLength(Float.toString(entry.getValue()));
        message.setRarity(fish.getRarity().getDisplayName());
        message.setFishCaught(fish.getDisplayName());
        message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_FISH);
        return message;
    }

    @Override
    public Message getSinglePlayerLeaderboard(Message message, CompetitionEntry entry) {
        Fish fish = entry.getFish();
        message.setRarityColour(fish.getRarity().getColour());
        message.setLength(Float.toString(entry.getValue()));
        message.setRarity(fish.getRarity().getDisplayName());
        message.setFishCaught(fish.getDisplayName());
        message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_FISH);
        return message;
    }
}
