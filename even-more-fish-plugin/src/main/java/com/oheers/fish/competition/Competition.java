package com.oheers.fish.competition;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.CompetitionConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.config.messages.Messages;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.database.UserReport;
import com.oheers.fish.fishing.FishingProcessor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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

            Function<Competition, @NotNull Boolean> typeBeginLogic = competitionType.getBeginLogic();
            if (typeBeginLogic != null && !typeBeginLogic.apply(this)) {
                active = false;
                return;
            }

            this.timeLeft = this.maxDuration;

            leaderboard = new Leaderboard(competitionType);

            if (showBar) {
                statusBar.setPrefix(FishUtils.translateColorCodes(CompetitionConfig.getInstance().getBarPrefix(competitionName)), competitionType);
                statusBar.show();
            }
            initTimer();
            announceBegin();
            EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(this);
            Bukkit.getServer().getPluginManager().callEvent(startEvent);
            epochStartTime = Instant.now().getEpochSecond();

            // Execute start commands
            List<String> startCommands = CompetitionConfig.getInstance().getCompetitionStartCommands(competitionName, adminStart);
            startCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

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

        switch (competitionType) {
            case SPECIFIC_FISH -> {
                message.setAmount(Integer.toString(numberNeeded));
                message.setRarityColour(selectedFish.getRarity().getColour());

                if (selectedFish.getRarity().getDisplayName() != null) {
                    message.setRarity(selectedFish.getRarity().getDisplayName());
                } else {
                    message.setRarity(selectedFish.getRarity().getValue());
                }

                if (selectedFish.getDisplayName() != null) {
                    message.setFishCaught(selectedFish.getDisplayName());
                } else {
                    message.setFishCaught(selectedFish.getName());
                }
            }
            case SPECIFIC_RARITY -> {
                message.setAmount(Integer.toString(numberNeeded));
                if (selectedRarity == null) {
                    EvenMoreFish.getInstance().getLogger().warning("Null rarity found. Please check your config files.");
                    return message;
                }
                message.setRarityColour(selectedRarity.getColour());

                if (selectedRarity.getDisplayName() != null) {
                    message.setRarity(selectedRarity.getDisplayName());
                } else {
                    message.setRarity(selectedRarity.getValue());
                }
            }
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
        if (
                competitionType == CompetitionType.SPECIFIC_FISH ||
                        competitionType == CompetitionType.SPECIFIC_RARITY ||
                        competitionType == CompetitionType.MOST_FISH ||
                        competitionType == CompetitionType.LARGEST_TOTAL ||
                        competitionType == CompetitionType.SHORTEST_TOTAL // New type
        ) {
            // is the fish the specific fish or rarity?
            if (competitionType == CompetitionType.SPECIFIC_FISH) {
                if (!(fish.getName().equalsIgnoreCase(selectedFish.getName()) && fish.getRarity() == selectedFish.getRarity())) {
                    return;
                }
            } else if (competitionType == CompetitionType.SPECIFIC_RARITY) {
                if (this.selectedRarity != null && !fish.getRarity().getValue().equals(this.selectedRarity.getValue())) {
                    return;
                }
            }

            if ((competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) && numberNeeded == 1) {
                singleReward(fisher);
                end(false);
            } else {
                CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

                float increaseAmount;
                if (this.competitionType == CompetitionType.LARGEST_TOTAL || this.competitionType == CompetitionType.SHORTEST_TOTAL) {
                    increaseAmount = fish.getLength();
                } else {
                    increaseAmount = 1.0f;
                }

                if (entry != null) {
                    if (entry.getValue() + increaseAmount >= leaderboard.getTopEntry().getValue() && leaderboard.getTopEntry().getPlayer() != fisher.getUniqueId()) {
                        Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
                        message.setLength(Float.toString(fish.getLength()));
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setFishCaught(fish.getName());
                        message.setRarity(fish.getRarity().getValue());
                        message.setPlayer(fisher);

                        FishUtils.broadcastFishMessage(message, fisher.getPlayer(), isDoingFirstPlaceActionBar());
                    }

                    try {
                        // re-adding the entry so it's sorted
                        leaderboard.removeEntry(entry);
                        entry.incrementValue(increaseAmount);
                        leaderboard.addEntry(entry);
                    } catch (Exception exception) {
                        EvenMoreFish.getInstance().getLogger().severe("Could not delete: " + entry);
                    }

                    if (entry.getValue() == numberNeeded && (competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY)) {
                        end(false);
                    }

                } else {
                    CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competitionType);
                    if (this.competitionType == CompetitionType.LARGEST_TOTAL) {
                        newEntry.incrementValue(fish.getLength() - 1);
                    } else if (this.competitionType == CompetitionType.SHORTEST_TOTAL) {
                        newEntry.incrementValue(fish.getLength() - 1); // Decrease for shortest total
                    }
                    leaderboard.addEntry(newEntry);
                }
            }
        } else {
            // If a fish has no size it shouldn't be able to join the competition
            if (fish.getLength() <= 0) {
                return;
            }

            CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

            if (entry != null) {
                if ((competitionType == CompetitionType.LARGEST_FISH && entry.getFish().getLength() < fish.getLength()) ||
                        (competitionType == CompetitionType.SHORTEST_FISH && entry.getFish().getLength() > fish.getLength())) {

                    if ((competitionType == CompetitionType.LARGEST_FISH && fish.getLength() > leaderboard.getTopEntry().getFish().getLength()) ||
                            (competitionType == CompetitionType.SHORTEST_FISH && fish.getLength() < leaderboard.getTopEntry().getFish().getLength()) &&
                                    leaderboard.getTopEntry().getPlayer() != fisher.getUniqueId()) {
                        Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
                        message.setLength(Float.toString(fish.getLength()));
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setFishCaught(fish.getName());
                        message.setRarity(fish.getRarity().getValue());
                        message.setPlayer(fisher);

                        FishUtils.broadcastFishMessage(message, fisher.getPlayer(), isDoingFirstPlaceActionBar());
                    }

                    leaderboard.removeEntry(entry);
                    leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competitionType));
                }

            } else {
                CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competitionType);

                if (leaderboard.getSize() != 0) {
                    if ((competitionType == CompetitionType.LARGEST_FISH && fish.getLength() > leaderboard.getTopEntry().getFish().getLength()) ||
                            (competitionType == CompetitionType.SHORTEST_FISH && fish.getLength() < leaderboard.getTopEntry().getFish().getLength()) &&
                                    leaderboard.getTopEntry().getPlayer() != fisher.getUniqueId()) {
                        Message message = new Message(ConfigMessage.NEW_FIRST_PLACE_NOTIFICATION);
                        message.setLength(Float.toString(fish.getLength()));
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setFishCaught(fish.getName());
                        message.setRarity(fish.getRarity().getValue());
                        message.setPlayer(fisher);

                        FishUtils.broadcastFishMessage(message, fisher.getPlayer(), isDoingFirstPlaceActionBar());
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

        message.broadcast();

        if (startSound != null) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), startSound, 10f, 1f));
        }
    }

    public void sendPlayerLeaderboard(Player player) {
        boolean reachingCount = true;

        if (!active) {
            new Message(ConfigMessage.NO_COMPETITION_RUNNING).broadcast(player);
            return;
        }

        if (leaderboard.getSize() == 0) {
            new Message(ConfigMessage.NO_FISH_CAUGHT).broadcast(player);
            return;
        }

        List<String> competitionColours = CompetitionConfig.getInstance().getPositionColours();
        StringBuilder builder = new StringBuilder();
        int pos = 0;

        List<CompetitionEntry> entries = new ArrayList<>(leaderboard.getEntries());

        // Sort entries in ascending order for SHORTEST_FISH
        if (competitionType == CompetitionType.SHORTEST_FISH) {
            entries.sort(Comparator.comparingDouble(entry -> entry.getFish().getLength()));
        }

        for (CompetitionEntry entry : entries) {
            pos++;
            if (reachingCount) {
                leaderboardMembers.add(entry.getPlayer());
                Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
                message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()));
                message.setPosition(Integer.toString(pos));
                if (pos > competitionColours.size()) {
                    Random r = EvenMoreFish.getInstance().getRandom();
                    int s = r.nextInt(3);
                    setPositionColour(s, message);
                } else {
                    message.setPositionColour(competitionColours.get(pos - 1));
                }

                switch (competitionType) {
                    case LARGEST_FISH, SHORTEST_FISH -> {
                        Fish fish = entry.getFish();
                        message.setRarityColour(fish.getRarity().getColour());
                        message.setLength(Float.toString(fish.getLength()));

                        if (fish.getRarity().getDisplayName() != null) {
                            message.setRarity(fish.getRarity().getDisplayName());
                        } else {
                            message.setRarity(fish.getRarity().getValue());
                        }

                        if (fish.getDisplayName() != null) {
                            message.setFishCaught(fish.getDisplayName());
                        } else {
                            message.setFishCaught(fish.getName());
                        }

                        message.setMessage(competitionType == CompetitionType.LARGEST_FISH ? ConfigMessage.LEADERBOARD_LARGEST_FISH : ConfigMessage.LEADERBOARD_SHORTEST_FISH);
                    }
                    case LARGEST_TOTAL -> {
                        message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_TOTAL);
                        message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                    }
                    case SHORTEST_TOTAL -> {
                        message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_TOTAL);
                        message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                    }
                    default -> {
                        message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
                        message.setAmount(Integer.toString((int) entry.getValue()));
                    }
                }
                builder.append(message.getRawMessage());

                if (pos == Messages.getInstance().getConfig().getInt("leaderboard-count")) {
                    if (Messages.getInstance().getConfig().getBoolean("always-show-pos")) {
                        if (leaderboardMembers.contains(player.getUniqueId())) {
                            break;
                        } else {
                            reachingCount = false;
                        }
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
                    message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()));
                    message.setPositionColour("&f");

                    switch (competitionType) {
                        case LARGEST_FISH, SHORTEST_FISH -> {
                            Fish fish = entry.getFish();
                            message.setRarityColour(fish.getRarity().getColour());
                            message.setLength(Float.toString(entry.getValue()));

                            if (fish.getRarity().getDisplayName() != null) {
                                message.setRarity(fish.getRarity().getDisplayName());
                            } else {
                                message.setRarity(fish.getRarity().getValue());
                            }

                            if (fish.getDisplayName() != null) {
                                message.setFishCaught(fish.getDisplayName());
                            } else {
                                message.setFishCaught(fish.getName());
                            }

                            message.setMessage(competitionType == CompetitionType.LARGEST_FISH ? ConfigMessage.LEADERBOARD_LARGEST_FISH : ConfigMessage.LEADERBOARD_SHORTEST_FISH);
                        }
                        case LARGEST_TOTAL -> {
                            message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_TOTAL);
                            message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                        }
                        case SHORTEST_TOTAL -> {
                            message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_TOTAL);
                            message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                        }
                        default -> {
                            message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
                            message.setAmount(Integer.toString((int) entry.getValue()));
                        }
                    }

                    builder.append("\n").append(message.getRawMessage());
                }
            }
        }
        player.sendMessage(builder.toString());
        Message message = new Message(ConfigMessage.LEADERBOARD_TOTAL_PLAYERS);
        message.setAmount(Integer.toString(leaderboard.getSize()));
        message.broadcast(player);
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

        List<String> competitionColours = CompetitionConfig.getInstance().getPositionColours();
        StringBuilder builder = new StringBuilder();
        int pos = 0;

        List<CompetitionEntry> entries = new ArrayList<>(leaderboard.getEntries());

        // Sort entries in ascending order for SHORTEST_FISH
        if (competitionType == CompetitionType.SHORTEST_FISH) {
            entries.sort(Comparator.comparingDouble(entry -> entry.getFish().getLength()));
        }

        for (CompetitionEntry entry : entries) {
            pos++;
            leaderboardMembers.add(entry.getPlayer());
            Message message = new Message(ConfigMessage.LEADERBOARD_LARGEST_FISH);
            message.setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()));
            message.setPosition(Integer.toString(pos));
            if (pos > competitionColours.size()) {
                Random r = EvenMoreFish.getInstance().getRandom();
                int s = r.nextInt(3);
                setPositionColour(s, message);
            } else {
                message.setPositionColour(competitionColours.get(pos - 1));
            }

            switch (competitionType) {
                case LARGEST_FISH -> {
                    Fish fish = entry.getFish();
                    message.setRarityColour(fish.getRarity().getColour());
                    message.setLength(Float.toString(entry.getValue()));

                    if (fish.getRarity().getDisplayName() != null) {
                        message.setRarity(fish.getRarity().getDisplayName());
                    } else {
                        message.setRarity(fish.getRarity().getValue());
                    }

                    if (fish.getDisplayName() != null) {
                        message.setFishCaught(fish.getDisplayName());
                    } else {
                        message.setFishCaught(fish.getName());
                    }

                    message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_FISH);
                }
                case SHORTEST_FISH -> {
                    Fish fish = entry.getFish();
                    message.setRarityColour(fish.getRarity().getColour());
                    message.setLength(Float.toString(entry.getValue()));

                    if (fish.getRarity().getDisplayName() != null) {
                        message.setRarity(fish.getRarity().getDisplayName());
                    } else {
                        message.setRarity(fish.getRarity().getValue());
                    }

                    if (fish.getDisplayName() != null) {
                        message.setFishCaught(fish.getDisplayName());
                    } else {
                        message.setFishCaught(fish.getName());
                    }

                    message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_FISH);
                }
                case LARGEST_TOTAL -> {
                    message.setMessage(ConfigMessage.LEADERBOARD_LARGEST_TOTAL);
                    message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                }
                case SHORTEST_TOTAL -> {
                    message.setMessage(ConfigMessage.LEADERBOARD_SHORTEST_TOTAL);
                    message.setAmount(Double.toString(Math.floor(entry.getValue() * 10) / 10));
                }
                default -> {
                    message.setMessage(ConfigMessage.LEADERBOARD_MOST_FISH);
                    message.setAmount(Integer.toString((int) entry.getValue()));
                }
            }
            builder.append(message.getRawMessage()).append("\n");
        }
        console.sendMessage(builder.toString());
        Message message = new Message(ConfigMessage.LEADERBOARD_TOTAL_PLAYERS);
        message.setAmount(Integer.toString(leaderboard.getSize()));
        message.broadcast(console);
    }

    public boolean chooseFish() {
        String competitionName = this.competitionName;
        boolean adminStart = this.adminStarted;
        List<String> configRarities = CompetitionConfig.getInstance().allowedRarities(competitionName, adminStart);

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competitionName + " competition config section.");
            return false;
        }

        List<Fish> fish = new ArrayList<>();
        List<Rarity> allowedRarities = new ArrayList<>();
        double totalWeight = 0;

        for (Rarity r : FishManager.getInstance().getRarityMap().keySet()) {
            if (configRarities.contains(r.getValue())) {
                fish.addAll(FishManager.getInstance().getRarityMap().get(r));
                allowedRarities.add(r);
                totalWeight += (r.getWeight());
            }
        }

        if (allowedRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("The allowed-rarities list found in the " + competitionName + " competition config contains no loaded rarities!");
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

        if (this.numberNeeded == 0) {
            setNumberNeeded(CompetitionConfig.getInstance().getNumberFishNeeded(competitionName, adminStart));
        }

        try {
            Fish selectedFish = FishManager.getInstance().getFish(allowedRarities.get(idx), null, null, 1.0d, null, false);
            if (selectedFish == null) {
                // For the catch block to catch.
                throw new IllegalArgumentException();
            }
            this.selectedFish = selectedFish;
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

    public boolean chooseRarity() {
        String competitionName = this.competitionName;
        boolean adminStart = this.adminStarted;
        List<String> configRarities = CompetitionConfig.getInstance().allowedRarities(competitionName, adminStart);

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competitionName + " competition config section.");
            return false;
        }

        setNumberNeeded(CompetitionConfig.getInstance().getNumberFishNeeded(competitionName, adminStart));

        try {
            String randomRarity = configRarities.get(new Random().nextInt(configRarities.size()));
            for (Rarity r : FishManager.getInstance().getRarityMap().keySet()) {
                if (r.getValue().equalsIgnoreCase(randomRarity)) {
                    this.selectedRarity = r;
                    return true;
                }
            }
            this.selectedRarity = FishManager.getInstance().randomWeightedRarity(null, 0, null, FishManager.getInstance().getRarityMap().keySet());
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance()
                    .getLogger()
                    .severe("Could not load: " + competitionName + " because a random rarity could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("rarities.size(): " + FishManager.getInstance().getRarityMap().keySet().size());
            EvenMoreFish.getInstance().getLogger().severe("configRarities.size(): " + configRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
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
            if (!((competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) && numberNeeded == 1)) {
                new Message(ConfigMessage.NO_WINNERS).broadcast();
            }
            return;
        }

        boolean databaseEnabled = MainConfig.getInstance().databaseEnabled();
        int rewardPlace = 1;

        // Sort entries in ascending order for SHORTEST_FISH
        List<CompetitionEntry> entries = new ArrayList<>(leaderboard.getEntries());
        if (competitionType == CompetitionType.SHORTEST_FISH) {
            entries.sort(Comparator.comparingDouble(entry -> entry.getFish().getLength()));
        }

        CompetitionEntry topEntry = entries.get(0);
        if (topEntry != null && databaseEnabled) {
            UserReport topReport = DataManager.getInstance().getUserReportIfExists(topEntry.getPlayer());
            if (topReport == null) {
                EvenMoreFish.getInstance().getLogger().severe("Could not fetch User Report for " + topEntry.getPlayer() + ", their data has not been modified.");
            } else {
                topReport.incrementCompetitionsWon(1);
            }
        }

        boolean participationRewardsExist = (participationRewards != null && !participationRewards.isEmpty());

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

    private void incrementCompetitionsJoined(CompetitionEntry entry) {
        UserReport report = DataManager.getInstance().getUserReportIfExists(entry.getPlayer());
        if (report != null) {
            report.incrementCompetitionsJoined(1);
            DataManager.getInstance().putUserReportCache(entry.getPlayer(), report);
        } else {
            EvenMoreFish.getInstance().getLogger().severe("User " + entry.getPlayer() + " does not exist in cache. ");
        }
    }

}
