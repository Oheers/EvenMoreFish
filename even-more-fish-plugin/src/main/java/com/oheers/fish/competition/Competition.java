package com.oheers.fish.competition;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.UserReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Competition {

    public static Leaderboard leaderboard;
    static boolean active;
    static boolean originallyRandom;
    private CompetitionType competitionType;
    private Fish selectedFish;
    private Rarity selectedRarity;
    private int numberNeeded;
    private String competitionName;
    private boolean adminStarted;
    private Message startMessage;
    long maxDuration;
    long timeLeft;
    Bar statusBar;
    boolean showBar;
    long epochStartTime;
    List<Long> alertTimes;
    Map<Integer, List<Reward>> rewards;
    int playersNeeded;
    Sound startSound;
    MyScheduledTask timingSystem;
    private CompetitionFile competitionFile;

    public Competition(final @NotNull CompetitionFile competitionFile) {
        // TODO instanceof AdminCompetitionFile
        if (false) {
            this.adminStarted = true;
        }
        this.competitionFile = competitionFile;
        this.competitionName = competitionFile.getId();
        this.playersNeeded = competitionFile.getPlayersNeeded();
        this.startSound = competitionFile.getStartSound();
        this.maxDuration = competitionFile.getDuration() * 60L;
        this.alertTimes = competitionFile.getAlertTimes();
        this.rewards = competitionFile.getRewards();
        this.competitionType = competitionFile.getType();
        this.statusBar = competitionFile.createBossbar();
    }

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
        try {
            if (!adminStart && EvenMoreFish.getInstance().getVisibleOnlinePlayers().size() < playersNeeded) {
                new Message(ConfigMessage.NOT_ENOUGH_PLAYERS).broadcast();
                active = false;
                return;
            }

            active = true;

            CompetitionStrategy strategy = competitionType.getStrategy();
            if (!strategy.begin(this)) {
                active = false;
                return;
            }

            this.timeLeft = this.maxDuration;

            leaderboard = new Leaderboard(competitionType);

            statusBar.show();

            initTimer();
            announceBegin();
            EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(this);
            Bukkit.getServer().getPluginManager().callEvent(startEvent);
            epochStartTime = Instant.now().getEpochSecond();

            // Execute start commands
            getCompetitionFile().getStartCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

            EvenMoreFish.getInstance().getDecidedRarities().clear();
        } catch (Exception ex) {
            end(true);
        }
    }

    public void end(boolean startFail) {
        // print leaderboard
        if (this.timingSystem != null) {
            this.timingSystem.cancel();
        }
        if (statusBar != null) {
            statusBar.hide();
        }
        try {
            if (!startFail) {
                EMFCompetitionEndEvent endEvent = new EMFCompetitionEndEvent(this);
                Bukkit.getServer().getPluginManager().callEvent(endEvent);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    new Message(ConfigMessage.COMPETITION_END).broadcast(player);
                    sendPlayerLeaderboard(player);
                }
                handleRewards();
                if (originallyRandom) {
                    competitionType = CompetitionType.RANDOM;
                }
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
                statusBar.timerUpdate(timeLeft, maxDuration);
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
        return competitionType.getStrategy().getTypeFormat(this, configMessage);
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

    /**
     * Calculates whether to send the "new first place" notification as an actionbar message or directly into chat.
     *
     * @return A boolean, true = do it in actionbar.
     */
    public boolean isDoingFirstPlaceActionBar() {
        boolean doActionBarMessage = Messages.getInstance().getConfig().getBoolean("action-bar-message");
        boolean isSupportedActionBarType = Messages.getInstance().getConfig().getStringList("action-bar-types").isEmpty() || Messages.getInstance()
                .getConfig()
                .getStringList("action-bar-types")
                .contains(EvenMoreFish.getInstance().getActiveCompetition().getCompetitionType().toString());
        return doActionBarMessage && isSupportedActionBarType;
    }

    public void applyToLeaderboard(Fish fish, Player fisher) {
        competitionType.getStrategy().applyToLeaderboard(fish, fisher, leaderboard, this);
    }

    public void announceBegin() {
        startMessage = competitionType.getStrategy().getBeginMessage(this, competitionType);
        startMessage.broadcast();

        if (startSound != null) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), startSound, 10f, 1f));
        }
    }

    private void setPositionColour(int place, Message message) {
        switch (place) {
            case 0 -> message.setPositionColour("&c» &r");
            case 1 -> message.setPositionColour("&c_ &r");
            case 2 -> message.setPositionColour("&c&ko &r");
        }
    }

    public void sendConsoleLeaderboard(ConsoleCommandSender console) {
        if (!active) {
            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(console);
            return;
        }
        if (leaderboard.getSize() == 0) {
            new Message(ConfigMessage.NO_FISH_CAUGHT).broadcast(console);
            return;
        }

        List<String> competitionColours = competitionFile.getPositionColours();
        List<CompetitionEntry> entries = getSortedEntries(leaderboard.getEntries());

        String leaderboardMessage = buildLeaderboardMessage(entries, competitionColours, true, null);
        console.sendMessage(leaderboardMessage);

        Message message = new Message(ConfigMessage.LEADERBOARD_TOTAL_PLAYERS);
        message.setAmount(Integer.toString(leaderboard.getSize()));
        message.broadcast(console);
    }

    public void sendPlayerLeaderboard(Player player) {
        if (!active) {
            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(player);
            return;
        }
        if (leaderboard.getSize() == 0) {
            new Message(ConfigMessage.NO_FISH_CAUGHT).broadcast(player);
            return;
        }

        List<String> competitionColours = competitionFile.getPositionColours();
        List<CompetitionEntry> entries = getSortedEntries(leaderboard.getEntries());

        String leaderboardMessage = buildLeaderboardMessage(entries, competitionColours, false, player.getUniqueId());
        player.sendMessage(leaderboardMessage);

        Message message = new Message(ConfigMessage.LEADERBOARD_TOTAL_PLAYERS);
        message.setAmount(Integer.toString(leaderboard.getSize()));
        message.broadcast(player);
    }

    public List<CompetitionEntry> getSortedEntries(List<CompetitionEntry> entries) {
        if (competitionType == CompetitionType.SHORTEST_FISH) {
            entries.sort(Comparator.comparingDouble(entry -> entry.getFish().getLength()));
        }
        return entries;
    }

    private String buildLeaderboardMessage(List<CompetitionEntry> entries, List<String> competitionColours, boolean isConsole, UUID playerUuid) {
        StringBuilder builder = new StringBuilder();
        int pos = 0;

        for (CompetitionEntry entry : entries) {
            pos++;
            Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
            message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()));
            message.setPosition(Integer.toString(pos));

            if (pos > competitionColours.size()) {
                int s = EvenMoreFish.getInstance().getRandom().nextInt(3);
                setPositionColour(s, message);
            } else {
                message.setPositionColour(competitionColours.get(pos - 1));
            }

            if (isConsole) {
                message = competitionType.getStrategy().getSingleConsoleLeaderboardMessage(message, entry);
            } else {
                message = competitionType.getStrategy().getSinglePlayerLeaderboard(message, entry);
                if (entry.getPlayer().equals(playerUuid)) {
                    message.setPositionColour("&f"); // Customize player-specific logic here if needed
                }
            }

            builder.append(message.getRawMessage()).append("\n");
        }

        return builder.toString();
    }

    private void handleDatabaseUpdates(CompetitionEntry entry, boolean isTopEntry) {
        if (!MainConfig.getInstance().databaseEnabled()) {
            return;
        }

        UserReport userReport = DataManager.getInstance().getUserReportIfExists(entry.getPlayer());
        if (userReport == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not fetch User Report for " + entry.getPlayer() + ", their data has not been modified.");
            return;
        }

        if (isTopEntry) {
            userReport.incrementCompetitionsWon(1);
        } else {
            userReport.incrementCompetitionsJoined(1);
        }
    }

    private void handleRewards() {
        if (leaderboard.getSize() == 0) {
            if (!((competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) && numberNeeded == 1)) {
                new Message(ConfigMessage.NO_WINNERS).broadcast();
            }
            return;
        }

        boolean databaseEnabled = MainConfig.getInstance().databaseEnabled();
        int rewardPlace = 1;

        List<CompetitionEntry> entries = getSortedEntries(leaderboard.getEntries());

        if (databaseEnabled && !entries.isEmpty()) {
            handleDatabaseUpdates(entries.get(0), true); // Top entry
        }

        for (CompetitionEntry entry : entries) {
            Player player = Bukkit.getPlayer(entry.getPlayer());

            // If the player is null, increment the place and continue
            if (player == null) {
                rewardPlace++;
                continue;
            }

            // Does the player's place have rewards?
            if (rewards.containsKey(rewardPlace)) {
                rewards.get(rewardPlace).forEach(reward -> reward.rewardPlayer(player, null));
            } else {
                // Default to participation rewards if not.
                List<Reward> participation = rewards.get(-1);
                if (participation != null) {
                    participation.forEach(reward -> reward.rewardPlayer(player, null));
                }
            }

            handleDatabaseUpdates(entry, false);

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

    public Message getStartMessage() {
        return startMessage;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public CompetitionFile getCompetitionFile() {
        return this.competitionFile;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public static Message getNextCompetitionMessage() {
        if (Competition.isActive()) {
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
        int competitionStartTime = EvenMoreFish.getInstance().getCompetitionQueue().getNextCompetition();
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

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public Fish getSelectedFish() {
        return selectedFish;
    }

    public void setSelectedFish(Fish selectedFish) {
        this.selectedFish = selectedFish;
    }

    public Rarity getSelectedRarity() {
        return selectedRarity;
    }

    public void setSelectedRarity(Rarity selectedRarity) {
        this.selectedRarity = selectedRarity;
    }

    public int getNumberNeeded() {
        return numberNeeded;
    }

    public boolean isAdminStarted() {
        return adminStarted;
    }

}
