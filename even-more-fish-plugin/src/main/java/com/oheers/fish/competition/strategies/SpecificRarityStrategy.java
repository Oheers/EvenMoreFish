package com.oheers.fish.competition.strategies;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class SpecificRarityStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        return chooseRarity(competition);
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        if (competition.selectedRarity != null &&
                !fish.getRarity().getValue().equals(competition.selectedRarity.getValue())) {
            return; // Fish doesn't match the required rarity
        }

        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());
        float increaseAmount = 1.0f;

        if (entry != null) {
            entry.incrementValue(increaseAmount);
            leaderboard.updateEntry(entry);
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
        }

        if (entry != null && entry.getValue() >= competition.numberNeeded) {
            competition.singleReward(fisher);
            competition.end(false);
        }
    }

    @Override
    public Message getBeginMessage(@NotNull Competition competition, CompetitionType type) {
        return getTypeFormat(competition, ConfigMessage.COMPETITION_START);
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

    private boolean chooseRarity(Competition competition) {
        List<String> configRarities = CompetitionConfig.getInstance().allowedRarities(competition.competitionName, competition.adminStarted);

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competition.competitionName + " competition config section.");
            return false;
        }

        competition.setNumberNeeded(CompetitionConfig.getInstance().getNumberFishNeeded(competition.competitionName, competition.adminStarted));

        try {
            String randomRarity = configRarities.get(EvenMoreFish.getInstance().getRandom().nextInt(configRarities.size()));
            Rarity rarity = FishManager.getInstance().getRarity(randomRarity);
            if (rarity != null) {
                competition.selectedRarity = rarity;
                return true;
            }
            competition.selectedRarity = FishManager.getInstance().getRandomWeightedRarity(null, 0, null, FishManager.getInstance().getRarityMap().keySet());
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance()
                    .getLogger()
                    .severe("Could not load: " + competition.competitionName + " because a random rarity could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("rarities.size(): " + FishManager.getInstance().getRarityMap().keySet().size());
            EvenMoreFish.getInstance().getLogger().severe("configRarities.size(): " + configRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }
}