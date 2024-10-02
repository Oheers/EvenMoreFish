package com.oheers.fish.competition.types.largest_fish;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.LeaderboardHandler;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LargestFishLeaderboard implements LeaderboardHandler {

    private List<CompetitionEntry> entries;
    private CompetitionType competitionType;

    public LargestFishLeaderboard(@NotNull CompetitionType type) {
        this.entries = new ArrayList<>();
        this.competitionType = type;
    }

    @Override
    public List<CompetitionEntry> getEntries() {
        return entries.stream()
                .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                .toList();
    }

    @Override
    public List<CompetitionEntry> getRawEntries() {
        return entries;
    }

    @Override
    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    @Override
    public void applyFish(@NotNull Fish fish, @NotNull Player player) {
        if (fish.getLength() <= 0) {
            return;
        }

        CompetitionEntry entry = getEntry(player.getUniqueId());
        if (entry == null) {
            entry = addEntry(player.getUniqueId(), fish);
        } else {
            entry.setValue(fish.getLength());
        }

        if (getSize() != 0 && entry.getValue() > getTopEntry().getValue()) {
            Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
            message.setLength(Float.toString(fish.getLength()));
            message.setRarityColour(fish.getRarity().getColour());
            message.setFishCaught(fish.getName());
            message.setRarity(fish.getRarity().getValue());
            message.setPlayer(player);
            FishUtils.broadcastFishMessage(message, player, CompetitionManager.getInstance().shouldUseActionBar(getCompetitionType()));
        }

    }

}
