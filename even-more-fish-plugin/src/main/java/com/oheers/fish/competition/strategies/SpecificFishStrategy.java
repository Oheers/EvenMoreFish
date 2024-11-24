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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SpecificFishStrategy implements CompetitionStrategy {
    @Override
    public boolean begin(Competition competition) {
        return chooseFish(competition);
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        if (!fish.getName().equalsIgnoreCase(competition.selectedFish.getName()) ||
                fish.getRarity() != competition.selectedFish.getRarity()) {
            return;
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
    public Message getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        Message message = CompetitionStrategy.super.getTypeFormat(competition, configMessage);
        message.setAmount(Integer.toString(competition.numberNeeded));
        message.setRarityColour(competition.selectedFish.getRarity().getColour());
        message.setRarity(competition.selectedFish.getRarity().getDisplayName());
        message.setFishCaught(competition.selectedFish.getDisplayName());
        return message;
    }

    @Override
    public Message getBeginMessage(@NotNull Competition competition, CompetitionType type) {
        return getTypeFormat(competition, ConfigMessage.COMPETITION_START);
    }

    private boolean chooseFish(Competition competition) {
        List<String> configRarities = CompetitionConfig.getInstance().allowedRarities(competition.competitionName, competition.adminStarted);

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competition.competitionName + " competition config section.");
            return false;
        }

        List<Fish> fish = new ArrayList<>();
        List<Rarity> allowedRarities = new ArrayList<>();
        double totalWeight = 0;

        for (String configRarity : configRarities) {
            Rarity rarity = FishManager.getInstance().getRarity(configRarity);
            if (rarity == null) {
                continue;
            }
            fish.addAll(FishManager.getInstance().getFishForRarity(rarity));
            allowedRarities.add(rarity);
            totalWeight += rarity.getWeight();
        }

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("The allowed-rarities list found in the " + competition.competitionName + " competition config contains no loaded rarities!");
            EvenMoreFish.getInstance().getLogger().severe("Configured Rarities: " + configRarities);
            EvenMoreFish.getInstance().getLogger().severe("Loaded Rarities: " + FishManager.getInstance().getRarityMap().keySet().stream().map(Rarity::getValue).toList());
            return false;
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            r -= allowedRarities.get(idx).getWeight();
            if (r <= 0.0) {
                break;
            }
        }

        if (competition.numberNeeded == 0) {
            competition.setNumberNeeded(CompetitionConfig.getInstance().getNumberFishNeeded(competition.competitionName, competition.adminStarted));
        }

        try {
            Fish selectedFish = FishManager.getInstance().getFish(allowedRarities.get(idx), null, null, 1.0d, null, false);
            if (selectedFish == null) {
                // For the catch block to catch.
                throw new IllegalArgumentException();
            }
            competition.selectedFish = selectedFish;
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            EvenMoreFish.getInstance()
                    .getLogger()
                    .severe("Could not load: " + competition.competitionName + " because a random fish could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("fish.size(): " + fish.size());
            EvenMoreFish.getInstance().getLogger().severe("allowedRarities.size(): " + allowedRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }


}