package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShortestFishStrategy implements CompetitionStrategy {

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        if (fish.getLength() <= 0) return;

        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

        if (entry != null) {
            if (fish.getLength() < entry.getFish().getLength()) {
                leaderboard.removeEntry(entry);
                leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
            }
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
        }
    }

    @Override
    public Message getSingleConsoleLeaderboardMessage(@NotNull Message message, @NotNull CompetitionEntry entry) {
        Fish fish = entry.getFish();
        message.setRarityColour(fish.getRarity().getColour());
        message.setLength("%.1f".formatted(entry.getValue()));
        message.setRarity(fish.getRarity().getDisplayName());
        message.setFishCaught(fish.getDisplayName());
        message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_FISH);
        return message;
    }

    @Override
    public Message getSinglePlayerLeaderboard(@NotNull Message message, @NotNull CompetitionEntry entry) {
        Fish fish = entry.getFish();
        message.setRarityColour(fish.getRarity().getColour());
        message.setLength("%.1f".formatted(entry.getValue()));
        message.setRarity(fish.getRarity().getDisplayName());
        message.setFishCaught(fish.getDisplayName());
        message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_FISH);
        return message;
    }
}
