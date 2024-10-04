package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.messages.ConfigMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum CompetitionType {
    LARGEST_FISH(
            ConfigMessage.COMPETITION_TYPE_LARGEST,
            "Largest Fish",
            false,
            null
    ),
    SPECIFIC_FISH(
            ConfigMessage.COMPETITION_TYPE_SPECIFIC,
            "Specific Fish",
            false,
            Competition::chooseFish
    ),
    MOST_FISH(
            ConfigMessage.COMPETITION_TYPE_MOST,
            "Most Fish",
            false,
            null
    ),
    SPECIFIC_RARITY(
            ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY,
            "Specific Rarity",
            false,
            Competition::chooseRarity
    ),
    LARGEST_TOTAL(
            ConfigMessage.COMPETITION_TYPE_LARGEST_TOTAL,
            "Largest Total",
            false,
            null
    ),
    RANDOM(
            // Use largest here, as there's no option for RANDOM
            ConfigMessage.COMPETITION_TYPE_LARGEST,
            "Random",
            false,
            competition -> {
                competition.competitionType = getRandomType();
                Competition.originallyRandom = true;
                return true;
            }
    ),
    SHORTEST_FISH(
            ConfigMessage.COMPETITION_TYPE_SHORTEST,
            "Shortest Fish",
            true,
            null
    ),
    SHORTEST_TOTAL(
            ConfigMessage.COMPETITION_TYPE_SHORTEST_TOTAL,
            "Shortest Total",
            true,
            null
    );

    private final ConfigMessage typeVariable;
    private final String barPrefix;
    private final boolean shouldReverseLeaderboard;
    private final Function<Competition, @NotNull Boolean> beginLogic;

    CompetitionType(ConfigMessage typeVariable, String barPrefix, boolean shouldReverseLeaderboard, Function<Competition, @NotNull Boolean> beginLogic) {
        this.typeVariable = typeVariable;
        this.barPrefix = barPrefix;
        this.shouldReverseLeaderboard = shouldReverseLeaderboard;
        this.beginLogic = beginLogic;
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

    public @Nullable Function<Competition, @NotNull Boolean> getBeginLogic() {
        return this.beginLogic;
    }

    public static CompetitionType getRandomType() {
        // -1 from the length so that the RANDOM isn't chosen as the random value.
        int type = EvenMoreFish.getInstance().getRandom().nextInt(values().length - 1);
        return values()[type];
    }

}
