package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.competition.types.RandomCompetitionType;
import com.oheers.fish.competition.types.largest_fish.LargestFishCompetitionType;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
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
        Competition comp = Competition.getActiveCompetition();
        if (comp != null) {
            comp.end(false);
        }
        Competition.stopActiveCompetition();
    }

    private void registerInternalTypes() {
        new RandomCompetitionType().register();
        new LargestFishCompetitionType().register();
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

    public @Nullable CompetitionType getRandomCompetitionType() {
        if (registeredTypes.isEmpty()) {
            return null;
        }
        try {
            // -1 from the length so that the RANDOM isn't chosen as the random value.
            int typePlace = EvenMoreFish.getInstance().getRandom().nextInt(registeredTypes.size() - 1);
            String[] typeNames = registeredTypes.keySet().toArray(String[]::new);
            return registeredTypes.get(typeNames[typePlace]);
        } catch (IndexOutOfBoundsException exception) {
            return null;
        }
    }

    public Map<String, CompetitionType> getRegisteredTypes() {
        return Map.copyOf(registeredTypes);
    }

    public boolean shouldUseActionBar(@NotNull CompetitionType competitionType) {
        boolean doActionBarMessage = Messages.getInstance().getConfig().getBoolean("action-bar-message");
        boolean isSupportedActionBarType = Messages.getInstance().getConfig().getStringList("action-bar-types").isEmpty() || Messages.getInstance()
                .getConfig()
                .getStringList("action-bar-types")
                .contains(competitionType.getIdentifier());
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

    public Message getNextCompetitionMessage() {
        if (Competition.isCurrentlyActive()) {
            return new Message(ConfigMessage.PLACEHOLDER_TIME_REMAINING_DURING_COMP);
        }

        int remainingTime = getRemainingTime();

        Message message = new Message(ConfigMessage.PLACEHOLDER_TIME_REMAINING);
        message.setDays(Integer.toString(remainingTime / 1440));
        message.setHours(Integer.toString((remainingTime % 1440) / 60));
        message.setMinutes(Integer.toString((((remainingTime % 1440) % 60) % 60)));

        return message;
    }

    private int getRemainingTime() {
        int competitionStartTime = CompetitionManager.getInstance().getCompetitionQueue().getNextCompetition();
        int currentTime = AutoRunner.getCurrentTimeCode();
        if (competitionStartTime > currentTime) {
            return competitionStartTime - currentTime;
        }

        return getRemainingTimeOverWeek(competitionStartTime, currentTime);
    }

    // time left of the current week + the time next week until next competition
    private int getRemainingTimeOverWeek(int competitionStartTime, int currentTime) {
        return (10080 - currentTime) + competitionStartTime;
    }

}
