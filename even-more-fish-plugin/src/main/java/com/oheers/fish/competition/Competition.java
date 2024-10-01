package com.oheers.fish.competition;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.leaderboard.LeaderboardHandler;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.UserReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

// TODO de-static this whole thing, move anything that needs to be moved to CompetitionManager
public class Competition {

    private LeaderboardHandler leaderboard;
    static boolean active;
    static boolean originallyRandom;
    private CompetitionType competitionType;
    private Fish selectedFish;
    private Rarity selectedRarity;
    private int numberNeeded;
    private String competitionName;
    private boolean adminStarted;
    private Message startMessage;
    long maxDuration, timeLeft;
    Bar statusBar;
    boolean showBar;
    long epochStartTime;
    List<Long> alertTimes;
    Map<Integer, List<Reward>> rewards;
    List<Reward> participationRewards;
    int playersNeeded;
    Sound startSound;
    MyScheduledTask timingSystem;
    List<UUID> leaderboardMembers = new ArrayList<>();
    private final List<String> beginCommands;

    public Competition(final Integer duration, final CompetitionType type, List<String> beginCommands) {
        this.maxDuration = duration;
        this.alertTimes = new ArrayList<>();
        this.rewards = new HashMap<>();
        this.competitionType = type;
        this.beginCommands = beginCommands;
    }

    public static void setOriginallyRandom(boolean originallyRandom) {
        Competition.originallyRandom = originallyRandom;
    }

    public void begin(boolean adminStart) {
        if (!adminStart && EvenMoreFish.getInstance().getOnlinePlayersExcludingVanish().size() < playersNeeded) {
            new Message(ConfigMessage.NOT_ENOUGH_PLAYERS).broadcast();
            active = false;
            return;
        }

        active = true;

        Function<Competition, Boolean> beginLogic = competitionType.getTypeBeginLogic();
        if (beginLogic != null && !beginLogic.apply(this)) {
            active = false;
            return;
        }

        this.timeLeft = this.maxDuration;

        leaderboard = competitionType.createLeaderboard();

        if (showBar) {
            statusBar.setPrefix(FishUtils.translateColorCodes(CompetitionConfig.getInstance().getBarPrefix(competitionName)), competitionType);
            statusBar.show();
        }

        initTimer();
        announceBegin();
        EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(startEvent);
        epochStartTime = Instant.now().getEpochSecond();
        this.beginCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

        // Players can have had their rarities decided to be a null rarity if the competition only check is disabled for some rarities
        EvenMoreFish.getInstance().getDecidedRarities().clear();
    }

    public void end(boolean startFail) {
        // print leaderboard
        if (this.timingSystem != null) {
            this.timingSystem.cancel();
        }
        if (statusBar != null) {
            statusBar.hide();
        }
        // try-catch-finally ensures the competition is ALWAYS marked as ended, so the next one will not break.
        try {
            if (!startFail) {
                EMFCompetitionEndEvent endEvent = new EMFCompetitionEndEvent(this);
                Bukkit.getServer().getPluginManager().callEvent(endEvent);
                new Message(ConfigMessage.COMPETITION_END).broadcast();
                leaderboard.sendToAll();
                handleRewards();
                if (MainConfig.getInstance().databaseEnabled()) {
                    Competition competitionRef = this;
                    EvenMoreFish.getScheduler().runTaskAsynchronously(() -> {
                        EvenMoreFish.getInstance().getDatabaseV3().createCompetitionReport(competitionRef);
                        leaderboard.clear();
                    });
                } else {
                    leaderboard.clear();
                }
            }
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "An exception was thrown while the competition was being ended!", exception);
        } finally {
            active = false;
        }
    }

    // Starts a runnable to decrease the time left by 1s each second
    private void initTimer() {
        this.timingSystem = new UniversalRunnable() {
            @Override
            public void run() {
                if (showBar) {
                    statusBar.timerUpdate(timeLeft, maxDuration);
                }
                if (decreaseTime()) {
                    cancel();
                }
            }
        }.runTaskTimer(EvenMoreFish.getInstance(), 0, 20);
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
            message.broadcast();
        } else if (timeLeft <= 0) {
            end(false);
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

        Consumer<Message> typeFormatLogic = competitionType.getTypeFormatLogic();
        if (typeFormatLogic != null) {
            typeFormatLogic.accept(message);
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
                if (processCompetitionSecond(timeLeft)) {
                    return true;
                }
                timeLeft--;
            }
        }
        return false;
    }

    public void announceBegin() {
        Message message = competitionType.getCompetitionStartMessage();
        startMessage = message;
        message.broadcast();
        if (startSound != null) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), startSound, 10f, 1f));
        }
    }

    public void initAlerts(String competitionName) {
        for (String s : CompetitionConfig.getInstance().getAlertTimes(competitionName)) {

            String[] split = s.split(":");
            if (split.length == 2) {
                try {
                    alertTimes.add((long) Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]));
                } catch (NumberFormatException nfe) {
                    EvenMoreFish.getInstance()
                            .getLogger()
                            .severe("Could not turn " + s + " into an alert time. If you need support, feel free to join the discord server: https://discord.gg/Hb9cj3tNbb");
                }
            } else {
                EvenMoreFish.getInstance().getLogger().severe(s + " is not formatted correctly. Use MM:SS");
            }
        }
    }

    public void initRewards(String competitionName, boolean adminStart) {
        Set<String> chosen;
        String path;

        // If the competition is an admin start or doesn't have its own rewards, we use the non-specific rewards, else we use the competition's reward config
        Set<String> positions = CompetitionConfig.getInstance().getRewardPositions(competitionName);
        if (adminStart || positions.isEmpty()) {
            chosen = CompetitionConfig.getInstance().getRewardPositions();
            path = "rewards.";
        } else {
            chosen = positions;
            path = "competitions." + competitionName + ".rewards.";
        }

        chosen.forEach(key -> {
            List<Reward> addingRewards = CompetitionConfig.getInstance().getStringList(path + key).stream()
                    .map(Reward::new)
                    .toList();

            if (key.equals("participation")) {
                this.participationRewards = addingRewards;
            } else {
                try {
                    this.rewards.put(Integer.parseInt(key), addingRewards);
                } catch (NumberFormatException exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.WARNING, key + " is not a valid number!", exception);
                }
            }
        });
    }

    private void handleRewards() {
        if (leaderboard.getSize() == 0) {
            new Message(ConfigMessage.NO_WINNERS).broadcast();
            return;
        }

        boolean databaseEnabled = MainConfig.getInstance().databaseEnabled();
        int rewardPlace = 1;
        CompetitionEntry topEntry = leaderboard.getTopEntry();
        if (topEntry != null && databaseEnabled) {
            UserReport topReport = DataManager.getInstance().getUserReportIfExists(topEntry.getPlayer());
            if (topReport == null) {
                EvenMoreFish.getInstance().getLogger().severe("Could not fetch User Report for " + topEntry.getPlayer() + ", their data has not been modified.");
            } else {
                topReport.incrementCompetitionsWon(1);
            }
        }

        boolean participationRewardsExist = (participationRewards != null && !participationRewards.isEmpty());

        for (CompetitionEntry entry : leaderboard.getEntries()) {

            Player player = Bukkit.getPlayer(entry.getPlayer());

            // If the player is null, increment the place and continue
            if (player == null) {
                rewardPlace++;
                continue;
            }

            // Does the player's place have rewards?
            if (rewards.containsKey(rewardPlace)) {
                rewards.get(rewardPlace).forEach(reward -> reward.rewardPlayer(player, null));
                // Default to participation rewards if not.
            } else {
                if (participationRewardsExist) {
                    participationRewards.forEach(reward -> reward.rewardPlayer(player, null));
                }
            }
            // Save to database if enabled
            if (databaseEnabled) {
                incrementCompetitionsJoined(entry);
            }
            // Increment the place
            rewardPlace++;
        }

    }

    public void singleReward(Player player) {
        Message message = getTypeFormat(ConfigMessage.COMPETITION_SINGLE_WINNER);
        message.setPlayer(player);
        message.setCompetitionType(competitionType);

        message.broadcast();

        if (!rewards.isEmpty()) {
            for (Reward reward : rewards.get(1)) {
                reward.rewardPlayer(player, null);
            }
        }
    }

    public void initBar(String competitionName) {

        showBar = CompetitionConfig.getInstance().getShowBar(competitionName);

        this.statusBar = new Bar();

        try {
            this.statusBar.setColour(BarColor.valueOf(CompetitionConfig.getInstance().getBarColour(competitionName).toUpperCase()));
        } catch (IllegalArgumentException iae) {
            EvenMoreFish.getInstance().getLogger().severe(CompetitionConfig.getInstance().getBarColour(competitionName) + " is not a valid bossbar colour, check ");
        }

        this.statusBar.setPrefix(FishUtils.translateColorCodes(CompetitionConfig.getInstance().getBarPrefix(competitionName)));
    }

    public void initGetNumbersNeeded(String competitionName) {
        this.playersNeeded = CompetitionConfig.getInstance().getPlayersNeeded(competitionName);
    }

    /**
     * The sound that gets sent to players when the competition begins, defined in competitions.yml
     *
     * @param competitionName The name of the competition as stated in the competitions.yml file.
     */
    public void initStartSound(String competitionName) {
        this.startSound = CompetitionConfig.getInstance().getStartNoise(competitionName);
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

    public LeaderboardHandler getLeaderboard() {
        return leaderboard;
    }

    public boolean isAdminStarted() {
        return this.adminStarted;
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

    // TODO move somewhere else
    public static Message getNextCompetitionMessage() {
        if (CompetitionManager.getInstance().isCompetitionActive()) {
            return new Message(ConfigMessage.PLACEHOLDER_TIME_REMAINING_DURING_COMP);
        }

        int remainingTime = getRemainingTime();

        Message message = new Message(ConfigMessage.PLACEHOLDER_TIME_REMAINING);
        message.setDays(Integer.toString(remainingTime / 1440));
        message.setHours(Integer.toString((remainingTime % 1440) / 60));
        message.setMinutes(Integer.toString((((remainingTime % 1440) % 60) % 60)));

        return message;
    }

    private static int getRemainingTime() {
        int competitionStartTime = CompetitionManager.getInstance().getCompetitionQueue().getNextCompetition();
        int currentTime = AutoRunner.getCurrentTimeCode();
        if (competitionStartTime > currentTime) {
            return competitionStartTime - currentTime;
        }

        return getRemainingTimeOverWeek(competitionStartTime, currentTime);
    }

    // time left of the current week + the time next week until next competition
    private static int getRemainingTimeOverWeek(int competitionStartTime, int currentTime) {
        return (10080 - currentTime) + competitionStartTime;
    }

    private void incrementCompetitionsJoined(CompetitionEntry entry) {
        UserReport report = DataManager.getInstance().getUserReportIfExists(entry.getPlayer());
        if (report != null) {
            report.incrementCompetitionsJoined(1);
            DataManager.getInstance().putUserReportCache(entry.getPlayer(), report);
        } else {
            EvenMoreFish.getInstance().getLogger().severe("User " + entry.getPlayer() + " does not exist in cache. ");
        }
    }

    public int getNumberNeeded() {
        return this.numberNeeded;
    }

    public void setSelectedFish(@NotNull Fish fish) {
        this.selectedFish = fish;
    }

    public void setSelectedRarity(@NotNull Rarity rarity) {
        this.selectedRarity = rarity;
    }

}
