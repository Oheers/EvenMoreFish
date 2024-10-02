package com.oheers.fish.competition.types.largest_fish;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.LeaderboardHandler;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class LargestFishCompetitionType implements CompetitionType {

    @Override
    public @NotNull String getIdentifier() {
        return "LARGEST_FISH";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

    @Override
    public @NotNull LeaderboardHandler createLeaderboard() {
        return new LargestFishLeaderboard(this);
    }

    @Override
    public @NotNull Message getMessageTypeVariable() {
        return new Message(ConfigMessage.COMPETITION_TYPE_LARGEST);
    }

    @Override
    public @Nullable Function<Competition, @NotNull Boolean> getTypeBeginLogic() {
        return null;
    }

    @Override
    public @Nullable Consumer<Message> getTypeFormatLogic() {
        return null;
    }

    @Override
    public @NotNull Message getCompetitionStartMessage() {
        Message message = new Message(ConfigMessage.COMPETITION_START);
        message.setCompetitionType(this);
        return message;
    }

    @Override
    public boolean shouldUseLength() {
        return true;
    }

    @Override
    public String getBarPrefix() {
        return "Largest Fish";
    }
}
