package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class CompetitionManager {

    private static CompetitionManager instance;

    private CompetitionQueue queue;
    private Map<String, CompetitionType> registeredTypes;
    private boolean loaded = false;
    private Competition activeCompetition;

    private CompetitionManager() {
        registeredTypes = new HashMap<>();
    }

    public static CompetitionManager getInstance() {
        if (instance == null) {
            instance = new CompetitionManager();
        }
        return instance;
    }

    public void load() {
        if (loaded) {
            return;
        }
        registerInternalTypes();
        queue = new CompetitionQueue();
        queue.load();
        AutoRunner.init();
    }

    public void reload() {
        if (!loaded) {
            return;
        }
        queue.load();
    }

    public void unload() {
        if (!loaded) {
            return;
        }
        getActiveCompetition().end(false);
    }

    private void registerInternalTypes() {

    }

    public boolean registerType(@NotNull CompetitionType type) {
        String identifier = type.getIdentifier().toUpperCase();
        if (registeredTypes.containsKey(type.getIdentifier())) {
            return false;
        }
        EMFPlugin.getLogger().info("Registered " + identifier + " CompetitionType by " + type.getAuthor() + " from the plugin " + type.getPlugin().getName());
        registeredTypes.put(identifier, type);
        return true;
    }

    public CompetitionQueue getCompetitionQueue() {
        return queue;
    }

    public @Nullable CompetitionType getCompetitionType(@NotNull String identifier) {
        return registeredTypes.get(identifier.toUpperCase());
    }

    public boolean isCompetitionActive() {
        return activeCompetition != null;
    }

    public boolean startCompetition(@NotNull Competition competition) {
        if (isCompetitionActive()) {
            return false;
        }
        this.activeCompetition = competition;
        activeCompetition.begin(false);
        return true;
    }

    public boolean endCompetition() {
        if (!isCompetitionActive()) {
            return false;
        }
        activeCompetition.end(false);
        activeCompetition = null;
        return true;
    }

    public Competition getActiveCompetition() {
        return activeCompetition;
    }

    public boolean shouldUseActionBar(@NotNull Competition competition) {
        boolean doActionBarMessage = Messages.getInstance().getConfig().getBoolean("action-bar-message");
        boolean isSupportedActionBarType = Messages.getInstance().getConfig().getStringList("action-bar-types").isEmpty() || Messages.getInstance()
                .getConfig()
                .getStringList("action-bar-types")
                .contains(competition.getCompetitionType().getIdentifier());
        return doActionBarMessage && isSupportedActionBarType;
    }

    public boolean chooseFish(@NotNull Competition competition) {
        String competitionName = competition.getCompetitionName();
        boolean adminStart = competition.isAdminStarted();

        List<String> configRarities = CompetitionConfig.getInstance().allowedRarities(competitionName, adminStart);

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competitionName + " competition config section.");
            return false;
        }

        List<Fish> fish = new ArrayList<>();
        List<Rarity> allowedRarities = new ArrayList<>();
        double totalWeight = 0;

        for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
            if (configRarities.contains(r.getValue())) {
                fish.addAll(EvenMoreFish.getInstance().getFishCollection().get(r));
                allowedRarities.add(r);
                totalWeight += (r.getWeight());
            }
        }

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("The allowed-rarities list found in the " + competitionName + " competition config contains no loaded rarities!");
            EvenMoreFish.getInstance().getLogger().severe("Configured Rarities: " + configRarities);
            EvenMoreFish.getInstance().getLogger().severe("Loaded Rarities: " + EvenMoreFish.getInstance().getFishCollection().keySet().stream().map(Rarity::getValue).toList());
            return false;
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            r -= allowedRarities.get(idx).getWeight();
            if (r <= 0.0) {
                break;
            }
        }

        if (competition.getNumberNeeded() == 0) {
            competition.setNumberNeeded(CompetitionConfig.getInstance().getNumberFishNeeded(competitionName, adminStart));
        }

        try {
            Fish selectedFish = FishingProcessor.getFish(allowedRarities.get(idx), null, null, 1.0d, null, false);
            if (selectedFish == null) {
                // For the catch block to catch.
                throw new IllegalArgumentException();
            }
            competition.setSelectedFish(selectedFish);
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            EvenMoreFish.getInstance()
                    .getLogger()
                    .severe("Could not load: " + competitionName + " because a random fish could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("fish.size(): " + fish.size());
            EvenMoreFish.getInstance().getLogger().severe("allowedRarities.size(): " + allowedRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }

    public boolean chooseRarity(@NotNull Competition competition) {
        String competitionName = competition.getCompetitionName();
        boolean adminStart = competition.isAdminStarted();

        List<String> configRarities = CompetitionConfig.getInstance().allowedRarities(competitionName, adminStart);

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competitionName + " competition config section.");
            return false;
        }

        competition.setNumberNeeded(CompetitionConfig.getInstance().getNumberFishNeeded(competitionName, adminStart));

        try {
            String randomRarity = configRarities.get(new Random().nextInt(configRarities.size()));
            for (Rarity r : EvenMoreFish.getInstance().getFishCollection().keySet()) {
                if (r.getValue().equalsIgnoreCase(randomRarity)) {
                    competition.setSelectedRarity(r);
                    return true;
                }
            }
            Rarity rarity = FishingProcessor.randomWeightedRarity(null, 0, null, EvenMoreFish.getInstance().getFishCollection().keySet());
            if (rarity == null) {
                return false;
            }
            competition.setSelectedRarity(rarity);
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance()
                    .getLogger()
                    .severe("Could not load: " + competitionName + " because a random rarity could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("rarities.size(): " + EvenMoreFish.getInstance().getFishCollection().keySet().size());
            EvenMoreFish.getInstance().getLogger().severe("configRarities.size(): " + configRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }

}
