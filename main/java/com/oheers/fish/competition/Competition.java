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

public class Competition {

    Integer maxDuration, timeLeft;
    CompetitionType competitionType;
    Bar statusBar;

    Fish selectedFish;

    BukkitTask timingSystem;

    String competitionName;
    String startMessage;

    static boolean active;

    // In a SPECIFIC_FISH competition, there won't be a leaderboard
    boolean leaderboardApplicable;
    public static LeaderboardTree leaderboard;

    public Competition(final Integer duration, final CompetitionType type) {
        this.maxDuration = duration;
        this.timeLeft = duration;
        this.competitionType = type;

        if (type != CompetitionType.SPECIFIC_FISH) {
            leaderboardApplicable = true;
            leaderboard = new LeaderboardTree();
            leaderboard.competitionType = competitionType;
        }
    }

    public void begin() {
        active = true;
        this.statusBar = new Bar();
        initTimer();
        announceBegin();
    }

    public void end() {
        // print leaderboard
        active = false;
        this.statusBar.removeAllPlayers();
        this.timingSystem.cancel();
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
        if (active && leaderboardApplicable) {
            leaderboard.addNode(fish, fisher);
        } else if (competitionType == CompetitionType.SPECIFIC_FISH) {
            if (fish.getName().equalsIgnoreCase(selectedFish.getName()) && fish.getRarity() == selectedFish.getRarity()) {
                end();
                // fisher.wins();
            }
        }
    }

    public void announceBegin() {
        String msg;
        if (this.competitionType != CompetitionType.SPECIFIC_FISH) {
            msg = new Message()
                    .setMSG(EvenMoreFish.msgs.getCompetitionStart())
                    .setType(this.competitionType)
                    .toString();
        } else {
            msg = new Message()
                    .setMSG(EvenMoreFish.msgs.getCompetitionStart())
                    .setType(this.competitionType)
                    .setRarity(selectedFish.getRarity().getValue())
                    .setColour(selectedFish.getRarity().getColour())
                    .setFishCaught(selectedFish.getName())
                    .toString();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(msg);
        }

        this.startMessage = msg;
    }

    public static String getLeaderboard(CompetitionType competitionType) {
        if (active) {
            if (leaderboard.size() != 0) {
                StringBuilder builder = new StringBuilder();
                int pos = 0;
                leaderboard.resetLeaderboard();
                for (Node node : leaderboard.getTopEntrants(leaderboard.root, EvenMoreFish.msgs.getLeaderboardCount())) {
                    pos++;
                    Fish fish = node.getFish();
                    Message message = new Message()
                            .setPositionColour(EvenMoreFish.msgs.getPosColour(pos))
                            .setPosition(Integer.toString(pos))
                            .setPlayer(node.getFisher().getName());
                    if (competitionType == CompetitionType.LARGEST_FISH) {
                        message.setRarity(fish.getRarity().getValue())
                                .setColour(fish.getRarity().getColour())
                                .setFishCaught(fish.getName())
                                .setLength(Float.toString(node.value))
                                .setMSG(EvenMoreFish.msgs.getLargestFishLeaderboard());
                    } else {
                        message.setAmount(Integer.toString((int) node.value))
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

    public LeaderboardTree getLeaderboardTree() {
        return leaderboard;
    }

    public Bar getStatusBar() {
        return this.statusBar;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public static boolean isActive() {
        return active;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void chooseFish(String competitionName, boolean adminStart) {
        List<String> allowedRarities = EvenMoreFish.competitionConfig.allowedRarities(competitionName, adminStart);
        List<Fish> fish = new ArrayList<>();
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (allowedRarities.contains(r.getValue())) {
                fish.addAll(EvenMoreFish.fishCollection.get(r));
            }
        }

        Random r = new Random();
        this.selectedFish = fish.get(r.nextInt(fish.size()));
    }
}
