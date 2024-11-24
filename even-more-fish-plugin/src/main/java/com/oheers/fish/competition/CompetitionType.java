package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.strategies.*;
import com.oheers.fish.config.messages.ConfigMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum CompetitionType {
    LARGEST_FISH(
            ConfigMessage.COMPETITION_TYPE_LARGEST,
            "Largest Fish",
            false,
            new LargestFishStrategy()
    ),
    SPECIFIC_FISH(
            ConfigMessage.COMPETITION_TYPE_SPECIFIC,
            "Specific Fish",
            false,
            new SpecificFishStrategy()
    ),
    MOST_FISH(
            ConfigMessage.COMPETITION_TYPE_MOST,
            "Most Fish",
            false,
            new MostFishStrategy()
    ),
    SPECIFIC_RARITY(
            ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY,
            "Specific Rarity",
            false,
            new SpecificRarityStrategy()
    ),
    LARGEST_TOTAL(
            ConfigMessage.COMPETITION_TYPE_LARGEST_TOTAL,
            "Largest Total",
            false,
            new LargestTotalStrategy()
    ),
    RANDOM(
            // Use largest here, as there's no option for RANDOM
            ConfigMessage.COMPETITION_TYPE_LARGEST,
            "Random",
            false,
            new RandomStrategy()
    ),
    SHORTEST_FISH(
            ConfigMessage.COMPETITION_TYPE_SHORTEST,
            "Shortest Fish",
            false,
            new ShortestFishStrategy()
    ),
    SHORTEST_TOTAL(
            ConfigMessage.COMPETITION_TYPE_SHORTEST_TOTAL,
            "Shortest Total",
            true,
            new ShortestTotalStrategy()
    );

    private final ConfigMessage typeVariable;
    private final String barPrefix;
    private final boolean shouldReverseLeaderboard;
    private final CompetitionStrategy strategy;

    CompetitionType(ConfigMessage typeVariable, String barPrefix, boolean shouldReverseLeaderboard, CompetitionStrategy strategy) {
        this.typeVariable = typeVariable;
        this.barPrefix = barPrefix;
        this.shouldReverseLeaderboard = shouldReverseLeaderboard;
        this.strategy = strategy;
    }

    public ConfigMessage getTypeVariable() {
        return this.typeVariable;
    }

    public String getBarPrefix() {
        return this.barPrefix;
    }

    public boolean shouldReverseLeaderboard() {
        return this.shouldReverseLeaderboard;
    }

    public CompetitionStrategy getStrategy() {
        return strategy;
    }




}
