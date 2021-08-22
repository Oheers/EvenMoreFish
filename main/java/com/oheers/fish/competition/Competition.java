package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.messages.Message;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class Competition {

    Integer maxDuration, timeLeft;
    CompetitionType competitionType;
    Bar statusBar;

    BukkitTask timingSystem;

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
        }
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
                                .setLength(Float.toString(node.getLength()))
                                .setMSG(EvenMoreFish.msgs.getLargestFishLeaderboard());
                    } else {
                        message.setAmount(Float.toString(node.value))
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

    public static boolean isActive() {
        return active;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }
}
