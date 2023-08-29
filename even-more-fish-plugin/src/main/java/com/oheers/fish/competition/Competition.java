package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.competition.reward.Reward;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.UserReport;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class Competition {

    public static Leaderboard leaderboard;
    static boolean active;
    static boolean originallyRandom;
    public CompetitionType competitionType;
    public Fish selectedFish;
    public Rarity selectedRarity;
    public int numberNeeded;
    public String competitionName;
    public boolean adminStarted;
    public String competitionID;
    public Message startMessage;
    long maxDuration, timeLeft;
    Bar statusBar;
    long epochStartTime;
    List<Long> alertTimes;
    Map<Integer, List<Reward>> rewards;
    List<Reward> participationRewards;
    int playersNeeded;
    Sound startSound;
    BukkitTask timingSystem;
    List<UUID> leaderboardMembers = new ArrayList<>();

    public Competition(final Integer duration, final CompetitionType type) {
        this.maxDuration = duration;
        this.alertTimes = new ArrayList<>();
        this.rewards = new HashMap<>();
        this.competitionType = type;
    }

    public static boolean isActive() {
        return active;
    }

    public static void setOriginallyRandom(boolean originallyRandom) {
        Competition.originallyRandom = originallyRandom;
    }

    public void begin(boolean adminStart) {
        if (Bukkit.getOnlinePlayers().size() >= playersNeeded || adminStart) {
            active = true;

            if (competitionType == CompetitionType.RANDOM) {
                competitionType = getRandomType();
                originallyRandom = true;
            }

            if (competitionType == CompetitionType.SPECIFIC_FISH) {
                if (!chooseFish(competitionName, adminStart)) return;
            }

            if (competitionType == CompetitionType.SPECIFIC_RARITY) {
                if (!chooseRarity(competitionName, adminStart)) return;
            }

            this.timeLeft = this.maxDuration;

            leaderboard = new Leaderboard(competitionType);
            statusBar.show();
            initTimer();
            announceBegin();
            EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(new Competition(Long.valueOf(this.maxDuration).intValue(), this.competitionType));
            Bukkit.getServer().getPluginManager().callEvent(startEvent);
            epochStartTime = Instant.now().getEpochSecond();

            // Players can have had their rarities decided to be a null rarity if the competition only check is disabled for some rarities
            EvenMoreFish.decidedRarities.clear();
        } else {
            new Message(ConfigMessage.NOT_ENOUGH_PLAYERS).broadcast(true, true);
            active = false;
        }
    }

    public void end() {
        active = false;
        // print leaderboard
        this.timingSystem.cancel();
        statusBar.hide();
        EMFCompetitionEndEvent endEvent = new EMFCompetitionEndEvent(new Competition(Long.valueOf(this.maxDuration).intValue(), this.competitionType));
        Bukkit.getServer().getPluginManager().callEvent(endEvent);
        for (Player player : Bukkit.getOnlinePlayers()) {
            new Message(ConfigMessage.COMPETITION_END).broadcast(player, true, true);
            sendPlayerLeaderboard(player);
        }
        handleRewards();
        if (originallyRandom) competitionType = CompetitionType.RANDOM;
        if (EvenMoreFish.mainConfig.databaseEnabled() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
            Competition competitionRef = this;
            new BukkitRunnable() {
            
                @Override
                public void run() {
                    EvenMoreFish.databaseV3.createCompetitionReport(competitionRef);
                    leaderboard.clear();
                }
            }.runTaskAsynchronously(JavaPlugin.getProvidingPlugin(Competition.class));
        } else {
            leaderboard.clear();
        }
    }

    // Starts a runnable to decrease the time left by 1s each second
    private void initTimer() {
        this.timingSystem = new BukkitRunnable() {
            @Override
            public void run() {
                statusBar.timerUpdate(timeLeft, maxDuration);
                if (decreaseTime()) cancel();
                //timeLeft--;
            }
        }.runTaskTimer(JavaPlugin.getProvidingPlugin(getClass()), 0, 20);
    }

    /**
     * Checks for scheduled alerts and whether the competition should end for each second - this is called automatically
     * by the competition ticker every 20 ticks.
     *
     * @param timeLeft How many seconds are left for the competition.
     * @return true if the competition is ending, false if not.
     */
    private boolean processCompetitionSecond(long timeLeft) {
        if (alertTimes.contains(timeLeft)) {
            Message message = getTypeFormat(ConfigMessage.TIME_ALERT);
            message.broadcast(true, true);

        } else if (timeLeft <= 0) {
            end();
            return true;
        }

        return false;
    }

    /**
     * This creates a message object and applies all the settings to it to make it able to use the {type} variable. It
     * takes into consideration whether it's a specific fish/rarity competition.
     *
     * @param configMessage The configmessage to use. Must have the {type} variable in it.
     * @return A message object that's pre-set to be compatible for the time remaining.
     */
    private Message getTypeFormat(ConfigMessage configMessage) {
        Message message = new Message(configMessage);
        message.setTimeFormatted(FishUtils.timeFormat(timeLeft));
        message.setTimeRaw(FishUtils.timeRaw(timeLeft));
        message.setCompetitionType(competitionType);

        if (competitionType == CompetitionType.SPECIFIC_FISH) {
            message.setAmount(Integer.toString(numberNeeded));
            message.setRarityColour(selectedFish.getRarity().getColour());

            if (selectedFish.getRarity().getDisplayName() != null)
                message.setRarity(selectedFish.getRarity().getDisplayName());
            else message.setRarity(selectedFish.getRarity().getValue());

            if (selectedFish.getDisplayName() != null) message.setFishCaught(selectedFish.getDisplayName());
            else message.setFishCaught(selectedFish.getName());
        } else if (competitionType == CompetitionType.SPECIFIC_RARITY) {
            message.setAmount(Integer.toString(numberNeeded));
            message.setRarityColour(selectedRarity.getColour());

            if (selectedRarity.getDisplayName() != null) message.setRarity(selectedRarity.getDisplayName());
            else message.setRarity(selectedRarity.getValue());
        }

        return message;
    }

    /**
     * On servers with low tps, 20 ticks != 1 second, so competitions can last for considerably longer, this re-calibrates
     * the time left with an epoch version of the time left. It runs through each second skipped to make sure all necessary
     * processes take place i.e. alerts.
     */
    private boolean decreaseTime() {
        long lagDif;
        long current = Instant.now().getEpochSecond();

        timeLeft = maxDuration - (current - epochStartTime);
        // +1 to counteract the seconds starting on 0 (or something like that)
        if ((lagDif = (current - epochStartTime) + 1) != maxDuration - timeLeft) {
            for (long i = maxDuration - timeLeft; i < lagDif; i++) {
                if (processCompetitionSecond(timeLeft)) return true;
                timeLeft--;
            }
        }
        return false;
    }

    /**
     * Calculates whether to send the "new first place" notification as an actionbar message or directly into chat.
     *
     * @return A boolean, true = do it in actionbar.
     */
    public boolean isDoingFirstPlaceActionBar() {
        boolean a = EvenMoreFish.msgs.getConfig().getBoolean("action-bar-message");
        boolean b = EvenMoreFish.msgs.getConfig().getStringList("action-bar-types").isEmpty() || EvenMoreFish.msgs.getConfig()
                .getStringList("action-bar-types").contains(EvenMoreFish.active.getCompetitionType().toString());
        return a && b;
    }

    public void applyToLeaderboard(Fish fish, Player fisher) {

        if (
                competitionType == CompetitionType.SPECIFIC_FISH ||
                        competitionType == CompetitionType.SPECIFIC_RARITY ||
                        competitionType == CompetitionType.MOST_FISH ||
                        competitionType == CompetitionType.LARGEST_TOTAL
        ) {
            // is the fish the specific fish or rarity?
            if (competitionType == CompetitionType.SPECIFIC_FISH) {
                if (!(fish.getName().equalsIgnoreCase(selectedFish.getName()) && fish.getRarity() == selectedFish.getRarity())) {
                    return;
                }
            } else if (competitionType == CompetitionType.SPECIFIC_RARITY) {
                if (!fish.getRarity().getValue().equals(this.selectedRarity.getValue())) {
                    return;
                }
            }

            if ((competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) && numberNeeded == 1) {
                singleReward(fisher);
                end();
            } else {
                CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

                float increaseAmount;
                if (this.competitionType == CompetitionType.LARGEST_TOTAL) increaseAmount = fish.getLength();
                else increaseAmount = 1.0f;

                if (entry != null) {
                    if (entry.getValue() + increaseAmount >= leaderboard.getTopEntry().getValue() && leaderboard.getTopEntry().getPlayer() != fisher.getUniqueId()) {
                        Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
                        message.setLength(Float.toString(fish.getLength()));
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setFishCaught(fish.getName());
                        message.setRarity(fish.getRarity().getValue());
                        message.setPlayer(fisher.getName());

                        FishUtils.broadcastFishMessage(message, isDoingFirstPlaceActionBar());
                    }

                    try {
                        // re-adding the entry so it's sorted
                        leaderboard.removeEntry(entry);
                        entry.incrementValue(increaseAmount);
                        leaderboard.addEntry(entry);
                    } catch (Exception exception) {
                        EvenMoreFish.logger.log(Level.SEVERE, "Could not delete: " + entry);
                    }

                    if (entry.getValue() == numberNeeded && (competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY)) {
                        end();
                    }

                } else {
                    CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competitionType);
                    if (this.competitionType == CompetitionType.LARGEST_TOTAL)
                        newEntry.incrementValue(fish.getLength() - 1);
                    leaderboard.addEntry(newEntry);
                }
            }
        } else {

            // If a fish has no size it shouldn't be able to join the competition
            if (fish.getLength() <= 0) return;

            CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

            if (entry != null) {

                if (entry.getFish().getLength() < fish.getLength()) {

                    if (fish.getLength() > leaderboard.getTopEntry().getFish().getLength() && leaderboard.getTopEntry().getPlayer() != fisher.getUniqueId()) {
                        Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
                        message.setLength(Float.toString(fish.getLength()));
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setFishCaught(fish.getName());
                        message.setRarity(fish.getRarity().getValue());
                        message.setPlayer(fisher.getName());

                        FishUtils.broadcastFishMessage(message, isDoingFirstPlaceActionBar());
                    }

                    leaderboard.removeEntry(entry);
                    leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competitionType));
                }

            } else {
                CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competitionType);

                if (leaderboard.getSize() != 0) {
                    if (fish.getLength() > leaderboard.getTopEntry().getFish().getLength() && leaderboard.getTopEntry().getPlayer() != fisher.getUniqueId()) {
                        Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
                        message.setLength(Float.toString(fish.getLength()));
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setFishCaught(fish.getName());
                        message.setRarity(fish.getRarity().getValue());
                        message.setPlayer(fisher.getName());

                        FishUtils.broadcastFishMessage(message, isDoingFirstPlaceActionBar());
                    }
                }

                leaderboard.addEntry(newEntry);
            }
        }
    }

    public void announceBegin() {
        Message message;

        if (competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) {
            message = getTypeFormat(ConfigMessage.COMPETITION_START);
        } else {
            message = new Message(ConfigMessage.COMPETITION_START);
            message.setCompetitionType(this.competitionType);
        }

        startMessage = message;

        boolean doingNoise = startSound != null;

        message.broadcast(true, true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (doingNoise) player.playSound(player.getLocation(), startSound, 10f, 1f);
        }
    }

    public void sendPlayerLeaderboard(Player player) {
        boolean reachingCount = true;
        if (active) {
            if (leaderboard.getSize() != 0) {

                List<String> competitionColours = EvenMoreFish.competitionConfig.getPositionColours();
                StringBuilder builder = new StringBuilder();
                int pos = 0;

                for (CompetitionEntry entry : leaderboard.getEntries()) {
                    pos++;
                    if (reachingCount) {
                        leaderboardMembers.add(entry.getPlayer());
                        Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
                        message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()).getName());
                        message.setPosition(Integer.toString(pos));
                        if (pos > competitionColours.size()) {
                            Random r = EvenMoreFish.getInstance().getRandom();
                            int s = r.nextInt(3);
                            switch (s) {
                                case 0:
                                    message.setPositionColour("&c\u00bb &r");
                                    break;
                                case 1:
                                    message.setPositionColour("&c_ &r");
                                    break;
                                case 2:
                                    message.setPositionColour("&c&ko &r");
                                    break;
                            }

                        } else message.setPositionColour(competitionColours.get(pos - 1));

                        if (competitionType == CompetitionType.LARGEST_FISH) {
                            Fish fish = entry.getFish();
                            message.setRarityColour(fish.getRarity().getColour());
                            message.setLength(Float.toString(entry.getValue()));

                            if (fish.getRarity().getDisplayName() != null)
                                message.setRarity(fish.getRarity().getDisplayName());
                            else message.setRarity(fish.getRarity().getValue());

                            if (fish.getDisplayName() != null) message.setFishCaught(fish.getDisplayName());
                            else message.setFishCaught(fish.getName());
                        } else {
                            if (competitionType == CompetitionType.LARGEST_TOTAL) {
                                message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_TOTAL);
                                // Clearing floating point .00000003 error not-cool stuff.
                                message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                            } else {
                                message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
                                message.setAmount(Integer.toString((int) entry.getValue()));
                            }

                        }
                        builder.append(message.getRawMessage(true, true));

                        if (pos == EvenMoreFish.msgs.getConfig().getInt("leaderboard-count")) {
                            if (EvenMoreFish.msgs.getConfig().getBoolean("always-show-pos")) {
                                if (leaderboardMembers.contains(player.getUniqueId())) break;
                                else reachingCount = false;
                            } else {
                                break;
                            }
                        } else {
                            builder.append("\n");
                        }
                    } else {
                        if (entry.getPlayer() == player.getUniqueId()) {
                            Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
                            message.setPosition(Integer.toString(pos));
                            message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()).getName());
                            message.setPositionColour("&f");

                            if (competitionType == CompetitionType.LARGEST_FISH) {
                                Fish fish = entry.getFish();
                                message.setRarityColour(fish.getRarity().getColour());
                                message.setLength(Float.toString(entry.getValue()));

                                if (fish.getRarity().getDisplayName() != null)
                                    message.setRarity(fish.getRarity().getDisplayName());
                                else message.setRarity(fish.getRarity().getValue());

                                if (fish.getDisplayName() != null) message.setFishCaught(fish.getDisplayName());
                                else message.setFishCaught(fish.getName());
                            } else {
                                message.setAmount(Integer.toString((int) entry.getValue()));
                                if (competitionType == CompetitionType.LARGEST_TOTAL)
                                    message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_TOTAL);
                                else message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
                            }

                            builder.append("\n").append(message.getRawMessage(true, true));
                        }
                    }
                }
                player.sendMessage(builder.toString());
                Message message = new Message(ConfigMessage.LEADERBOARD_TOTAL_PLAYERS);
                message.setAmount(Integer.toString(leaderboard.getSize()));
                message.broadcast(player, true, true);
            } else {
                new Message(ConfigMessage.NO_FISH_CAUGHT).broadcast(player, true, false);
            }
        } else {
            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(player, true, true);
        }
    }

    public void sendConsoleLeaderboard(ConsoleCommandSender console) {
        boolean reachingCount = true;
        if (active) {
            if (leaderboard.getSize() != 0) {

                List<String> competitionColours = EvenMoreFish.competitionConfig.getPositionColours();
                StringBuilder builder = new StringBuilder();
                int pos = 0;

                for (CompetitionEntry entry : leaderboard.getEntries()) {
                    pos++;
                    leaderboardMembers.add(entry.getPlayer());
                    Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
                    message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()).getName());
                    message.setPosition(Integer.toString(pos));
                    if (pos > competitionColours.size()) {
                        Random r = new Random();
                        int s = r.nextInt(3);
                        switch (s) {
                            case 0:
                                message.setPositionColour("&c\u00bb &r");
                                break;
                            case 1:
                                message.setPositionColour("&c_ &r");
                                break;
                            case 2:
                                message.setPositionColour("&c&ko &r");
                                break;
                        }

                    } else message.setPositionColour(competitionColours.get(pos - 1));

                    if (competitionType == CompetitionType.LARGEST_FISH) {
                        Fish fish = entry.getFish();
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setLength(Float.toString(entry.getValue()));

                        if (fish.getRarity().getDisplayName() != null)
                            message.setRarity(fish.getRarity().getDisplayName());
                        else message.setRarity(fish.getRarity().getValue());

                        if (fish.getDisplayName() != null) message.setFishCaught(fish.getDisplayName());
                        else message.setFishCaught(fish.getName());
                    } else {
                        if (competitionType == CompetitionType.LARGEST_TOTAL) {
                            message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_TOTAL);
                            // Clearing floating point .00000003 error not-cool stuff.
                            message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                        } else {
                            message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
                            message.setAmount(Integer.toString((int) entry.getValue()));
                        }

                    }
                    builder.append(message.getRawMessage(true, true));

                }
                console.sendMessage(builder.toString());
                Message message = new Message(ConfigMessage.LEADERBOARD_TOTAL_PLAYERS);
                message.setAmount(Integer.toString(leaderboard.getSize()));
                message.broadcast(console, true, true);
            } else {
                new Message(ConfigMessage.NO_FISH_CAUGHT).broadcast(console, true, false);
            }
        } else {
            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(console, true, true);
        }
    }

    public boolean chooseFish(String competitionName, boolean adminStart) {
        List<String> configRarities = EvenMoreFish.competitionConfig.allowedRarities(competitionName, adminStart);

        if (configRarities.isEmpty()) {
            EvenMoreFish.logger.log(Level.SEVERE, "No allowed-rarities list found in the " + competitionName + " competition config section.");
            return false;
        }

        List<Fish> fish = new ArrayList<>();
        List<Rarity> allowedRarities = new ArrayList<>();
        double totalWeight = 0;

        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (configRarities.contains(r.getValue())) {
                fish.addAll(EvenMoreFish.fishCollection.get(r));
                allowedRarities.add(r);
                totalWeight += (r.getWeight());
            }
        }

        configRarities = null;

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            r -= allowedRarities.get(idx).getWeight();
            if (r <= 0.0) break;
        }

        setNumberNeeded(EvenMoreFish.competitionConfig.getNumberFishNeeded(competitionName, adminStart));

        try {
            this.selectedFish = FishingProcessor.getFish(allowedRarities.get(idx), null, null, 1.0d, null, false);
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not load: " + competitionName + " because a random fish could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.logger.log(Level.SEVERE, "fish.size(): " + fish.size());
            EvenMoreFish.logger.log(Level.SEVERE, "allowedRarities.size(): " + configRarities.size());
            return false;
        }
    }

    public boolean chooseRarity(String competitionName, boolean adminStart) {
        List<String> configRarities = EvenMoreFish.competitionConfig.allowedRarities(competitionName, adminStart);

        if (configRarities.isEmpty()) {
            EvenMoreFish.logger.log(Level.SEVERE, "No allowed-rarities list found in the " + competitionName + " competition config section.");
            return false;
        }

        setNumberNeeded(EvenMoreFish.competitionConfig.getNumberFishNeeded(competitionName, adminStart));

        try {
            String randomRarity = configRarities.get(new Random().nextInt(configRarities.size()));
            for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
                if (r.getValue().equalsIgnoreCase(randomRarity)) {
                    this.selectedRarity = r;
                    return true;
                }
            }
            this.selectedRarity = FishingProcessor.randomWeightedRarity(null, 0, null, EvenMoreFish.fishCollection.keySet());
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "Could not load: " + competitionName + " because a random rarity could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.logger.log(Level.SEVERE, "rarities.size(): " + EvenMoreFish.fishCollection.keySet().size());
            EvenMoreFish.logger.log(Level.SEVERE, "allowedRarities.size(): " + configRarities.size());
            return false;
        }
    }

    public void initAlerts(String competitionName) {
        for (String s : EvenMoreFish.competitionConfig.getAlertTimes(competitionName)) {

            String[] split = s.split(":");
            if (split.length == 2) {
                try {
                    alertTimes.add((long) Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]));
                } catch (NumberFormatException nfe) {
                    EvenMoreFish.logger.log(Level.SEVERE, "Could not turn " + s + " into an alert time. If you need support, feel free to join the discord server: https://discord.gg/Hb9cj3tNbb");
                }
            } else {
                EvenMoreFish.logger.log(Level.SEVERE, s + " is not formatted correctly. Use MM:SS");
            }
        }
    }

    public void initRewards(String competitionName, boolean adminStart) {
        Set<String> chosen;
        String path;

        // If the competition is an admin start or doesn't have its own rewards, we use the non-specific rewards, else we use the compeitions
        if (adminStart) {
            chosen = EvenMoreFish.competitionConfig.getRewardPositions();
            path = "rewards.";
        } else {
            if (EvenMoreFish.competitionConfig.getRewardPositions(competitionName).isEmpty()) {
                chosen = EvenMoreFish.competitionConfig.getRewardPositions();
                path = "rewards.";
            } else {
                chosen = EvenMoreFish.competitionConfig.getRewardPositions(competitionName);
                path = "competitions." + competitionName + ".rewards.";
            }
        }

        if (chosen != null) {
            for (String i : chosen) {
                List<Reward> addingRewards = new ArrayList<>();
                for (String j : EvenMoreFish.competitionConfig.getStringList(path + i)) {
                    Reward reward = new Reward(j);
                    addingRewards.add(reward);
                }

                if (Objects.equals(i, "participation")) this.participationRewards = addingRewards;
                else this.rewards.put(Integer.parseInt(i), addingRewards);
            }
        }
    }

    private void handleRewards() {
        if (leaderboard.getSize() != 0) {
            Iterator<CompetitionEntry> iterator = leaderboard.getIterator();
            int i = 1;
            CompetitionEntry topEntry = leaderboard.getTopEntry();
            if (topEntry != null) {
                if (EvenMoreFish.mainConfig.databaseEnabled() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
                    UserReport report = DataManager.getInstance().getUserReportIfExists(topEntry.getPlayer());
                    if (report != null) {
                        report.incrementCompetitionsWon(1);
                    } else {
                        EvenMoreFish.logger.log(Level.SEVERE, "Could not fetch User Report for " + topEntry.getPlayer() + ", their data has not been modified.");
                    }
                }
            }

            while (iterator.hasNext()) {
                if (i <= rewards.size()) {
                    CompetitionEntry entry = iterator.next();
                    for (Reward reward : rewards.get(i)) {
                        reward.run(Bukkit.getOfflinePlayer(entry.getPlayer()), null);
                    }
                    i++;
                    if (EvenMoreFish.mainConfig.databaseEnabled() && EvenMoreFish.mainConfig.doingExperimentalFeatures() && entry != null) {
                        UserReport report = DataManager.getInstance().getUserReportIfExists(entry.getPlayer());
                        if (report != null) {
                            report.incrementCompetitionsJoined(1);
                            DataManager.getInstance().putUserReportCache(entry.getPlayer(), report);
                        }
                        else {
                            EvenMoreFish.logger.log(Level.SEVERE, "Could not increment competitions won for " + entry.getPlayer());
                        }
                    }
                } else {
                    if (participationRewards != null) {
                        iterator.forEachRemaining(competitionEntry -> {
                            for (Reward reward : participationRewards) {
                                reward.run(Bukkit.getOfflinePlayer(competitionEntry.getPlayer()), null);
                            }

                            if (EvenMoreFish.mainConfig.databaseEnabled() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
                                UserReport report = DataManager.getInstance().getUserReportIfExists(competitionEntry.getPlayer());
                                if (report != null) {
                                    report.incrementCompetitionsJoined(1);
                                    DataManager.getInstance().putUserReportCache(competitionEntry.getPlayer(), report);
                                } else {
                                    EvenMoreFish.logger.log(Level.SEVERE, "User " + competitionEntry.getPlayer() + " does not exist in cache. ");
                                }
                            }
                        });
                    } else if (EvenMoreFish.mainConfig.databaseEnabled() && EvenMoreFish.mainConfig.doingExperimentalFeatures()) {
                        iterator.forEachRemaining(competitionEntry -> {
                            UserReport report = DataManager.getInstance().getUserReportIfExists(competitionEntry.getPlayer());
                            if (report != null) {
                                report.incrementCompetitionsJoined(1);
                                DataManager.getInstance().putUserReportCache(competitionEntry.getPlayer(), report);
                            } else {
                                EvenMoreFish.logger.log(Level.SEVERE, "User " + competitionEntry.getPlayer() + " does not exist in cache. ");
                            }
                        });
                    }
                }
            }

        } else {
            if (!((competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) && numberNeeded == 1)) {
                new Message(ConfigMessage.NO_WINNERS).broadcast(true, false);
            }
        }
    }

    public void singleReward(Player player) {
        Message message = getTypeFormat(ConfigMessage.COMPETITION_SINGLE_WINNER);
        message.setPlayer(player.getName());
        message.setCompetitionType(competitionType);

        message.broadcast(true, true);

        if (!rewards.isEmpty()) {
            for (Reward reward : rewards.get(1)) {
                reward.run(player, null);
            }
        }
    }

    public void initBar(String competionName) {
        this.statusBar = new Bar();

        try {
            this.statusBar.setColour(BarColor.valueOf(EvenMoreFish.competitionConfig.getBarColour(competionName)));
        } catch (IllegalArgumentException iae) {
            EvenMoreFish.logger.log(Level.SEVERE, EvenMoreFish.competitionConfig.getBarColour(competionName) + " is not a valid bossbar colour, check ");
        }

        this.statusBar.setPrefix(FishUtils.translateHexColorCodes(EvenMoreFish.competitionConfig.getBarPrefix(competionName)));
    }

    public void initGetNumbersNeeded(String competitionName) {
        this.playersNeeded = EvenMoreFish.competitionConfig.getPlayersNeeded(competitionName);
    }

    /**
     * The sound that gets sent to players when the competition begins, defined in competitions.yml
     *
     * @param competitionName The name of the competition as stated in the competitions.yml file.
     */
    public void initStartSound(String competitionName) {
        this.startSound = EvenMoreFish.competitionConfig.getStartNoise(competitionName);
    }

    private CompetitionType getRandomType() {
        // -1 from the length so that the RANDOM isn't chosen as the random value.
        int type = new Random().nextInt(CompetitionType.values().length - 1);
        return CompetitionType.values()[type];
    }

    public Bar getStatusBar() {
        return this.statusBar;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setNumberNeeded(int numberNeeded) {
        this.numberNeeded = numberNeeded;
    }

    public int getLeaderboardSize() {
        return leaderboard.getSize();
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public void setAdminStarted(boolean adminStarted) {
        this.adminStarted = adminStarted;
    }

    public Message getStartMessage() {
        return startMessage;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }
}
