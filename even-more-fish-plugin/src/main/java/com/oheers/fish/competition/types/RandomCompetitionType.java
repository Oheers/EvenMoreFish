package com.oheers.fish.competition.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.LeaderboardHandler;
import com.oheers.fish.config.messages.Message;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class RandomCompetitionType implements CompetitionType {

    private CompetitionType safetyType;

    @Override
    public @NotNull String getIdentifier() {
        return "RANDOM";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

    // This method should not be accessible with this specific type.
    @Override
    public @Nullable LeaderboardHandler createLeaderboard() {
        return safetyType.createLeaderboard();
    }

    // This method should not be accessible with this specific type.
    @Override
    public @NotNull Message getMessageTypeVariable() {
        return safetyType.getMessageTypeVariable();
    }

    @Override
    public @Nullable Function<Competition, @NotNull Boolean> getTypeBeginLogic() {
        return competition -> {
            CompetitionType randomType = CompetitionManager.getInstance().getRandomCompetitionType();
            if (randomType == null || randomType.getIdentifier().equalsIgnoreCase(getIdentifier())) {
                EvenMoreFish.getInstance().getLogger().warning("Could not find a valid CompetitionType!");
                return false;
            }
            // Just to be safe, keep a valid CompetitionType to use for this type's methods.
            // The methods should not be accessible, so we can use any for this.
            this.safetyType = randomType;
            competition.setCompetitionType(randomType);
            return true;
        };
    }

    // This method should not be accessible with this specific type.
    @Override
    public @Nullable Consumer<Message> getTypeFormatLogic() {
        return safetyType.getTypeFormatLogic();
    }

    // This method should not be accessible with this specific type.
    @Override
    public @NotNull Message getCompetitionStartMessage() {
        return safetyType.getCompetitionStartMessage();
    }

    // This method should not be accessible with this specific type.
    @Override
    public boolean shouldUseLength() {
        return safetyType.shouldUseLength();
    }

    @Override
    public String getBarPrefix() {
        return "Random";
    }

}
