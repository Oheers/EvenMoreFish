package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class Competition {

    Integer maxDuration, timeLeft;
    CompetitionType competitionType;
    Bar statusBar;

    Fish selectedFish;
    int numberNeeded;

    BukkitTask timingSystem;

    Message startMessage;

    static boolean active;

    // In a SPECIFIC_FISH competition, there won't be a leaderboard
    boolean leaderboardApplicable;
    public static Leaderboard leaderboard;

    public Competition(final Integer duration, final CompetitionType type) {
        this.maxDuration = duration;
        this.timeLeft = duration;
        this.competitionType = type;
    }

    public void begin() {
        active = true;
        this.statusBar = new Bar();
        if (leaderboardApplicable) initLeaderboard();
        initTimer();
        announceBegin();
    }

    public void end() {
        // print leaderboard
        active = false;
        this.statusBar.removeAllPlayers();
        this.timingSystem.cancel();
        leaderboard.clear();
    }

    // Starts an async task to decrease the time left by 1s each second
    private void initTimer() {
        this.timingSystem = new BukkitRunnable() {
            @Override
            public void run() {
                timeLeft--;

                if (timeLeft == 300) {
                    System.out.println("5m left lads come on chop chop.");
                } else if (timeLeft == 0) {
                    end();
                    return;
                }
                statusBar.timerUpdate(timeLeft, maxDuration);

            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("EvenMoreFish")), 0, 20);
    }

    public void applyToLeaderboard(Fish fish, Player fisher) {

        if (competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.MOST_FISH) {
            // is the fish the specific fish?
            if (!(fish.getName().equalsIgnoreCase(selectedFish.getName()) && fish.getRarity() == selectedFish.getRarity())) {
                if (competitionType == CompetitionType.SPECIFIC_FISH) {
                    return;
                }
            }

            if (leaderboardApplicable) {
                CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

                if (entry != null) {

                    try {
                        // re-adding the entry so it's sorted
                        leaderboard.removeEntry(entry);
                        entry.incrementValue();
                        leaderboard.addEntry(entry);
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "Could not delete: " + entry);
                    }

                    if (entry.getValue() == numberNeeded && competitionType == CompetitionType.SPECIFIC_FISH) {
                        end();
                        // fisher wins
                    }

                } else {
                    CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competitionType);
                    leaderboard.addEntry(newEntry);
                }
            } else {
                end();
                // fisher wins
            }
        }
    }

    public void announceBegin() {
        Message msg;
        if (this.competitionType != CompetitionType.SPECIFIC_FISH) {
            msg = new Message()
                    .setMSG(EvenMoreFish.msgs.getCompetitionStart())
                    .setType(this.competitionType);
        } else {
            msg = new Message()
                    .setMSG(EvenMoreFish.msgs.getCompetitionStart())
                    .setAmount(Integer.toString(this.numberNeeded))
                    .setType(this.competitionType)
                    .setRarity(selectedFish.getRarity().getValue())
                    .setColour(selectedFish.getRarity().getColour())
                    .setFishCaught(selectedFish.getName());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg.toString());
        }

        this.startMessage = msg;
    }

    public static String getLeaderboard(CompetitionType competitionType) {
        if (active) {
            if (leaderboard.getSize() != 0) {

                List<String> competitionColours = EvenMoreFish.competitionConfig.getPositionColours();
                StringBuilder builder = new StringBuilder();
                int pos = 0;

                for (CompetitionEntry entry : leaderboard.getEntries()) {
                    pos++;
                    Message message = new Message()
                            .setPosition(Integer.toString(pos))
                            .setPlayer(Bukkit.getOfflinePlayer(entry.getPlayer()).getName());

                    if (pos > competitionColours.size()) {
                        Random r = new Random();
                        int s = r.nextInt(3);
                        switch (s) {
                            case 0: message.setPositionColour("&c\u00bb &r"); break;
                            case 1: message.setPositionColour("&c_ &r"); break;
                            case 2: message.setPositionColour("&c&ko &r"); break;
                        }

                    }
                    else message.setPositionColour(competitionColours.get(pos-1));

                    if (competitionType == CompetitionType.LARGEST_FISH) {
                        Fish fish = entry.getFish();
                        message.setRarity(fish.getRarity().getValue())
                                .setColour(fish.getRarity().getColour())
                                .setFishCaught(fish.getName())
                                .setLength(Float.toString(entry.getValue()))
                                .setMSG(EvenMoreFish.msgs.getLargestFishLeaderboard());
                    } else {
                        message.setAmount(Integer.toString((int) entry.getValue()))
                                .setMSG(EvenMoreFish.msgs.getMostFishLeaderboard());
                    }
                    builder.append(message);
                    if (pos != EvenMoreFish.msgs.getLeaderboardCount()) {
                        builder.append("\n");
                    }
                }
                return builder.toString();
            } else {
                return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.noFish());
            }
        } else {
            return FishUtils.translateHexColorCodes(EvenMoreFish.msgs.competitionNotRunning());
        }
    }

    public Bar getStatusBar() {
        return this.statusBar;
    }

    public Message getStartMessage() {
        return startMessage;
    }

    public static boolean isActive() {
        return active;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setNumberNeeded(int numberNeeded) {
        this.numberNeeded = numberNeeded;
    }

    private void initLeaderboard() {
        leaderboardApplicable = true;
        leaderboard = new Leaderboard(competitionType);
    }

    public void chooseFish(String competitionName, boolean adminStart) {
        List<String> allowedRarities = EvenMoreFish.competitionConfig.allowedRarities(competitionName, adminStart);
        List<Fish> fish = new ArrayList<>();
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (allowedRarities.contains(r.getValue())) {
                fish.addAll(EvenMoreFish.fishCollection.get(r));
            }
        }

        int y = EvenMoreFish.competitionConfig.getNumberFishNeeded(competitionName, adminStart);
        if (y > 1) initLeaderboard();
        setNumberNeeded(y);

        Random r = new Random();
        this.selectedFish = fish.get(r.nextInt(fish.size()));
    }
}
