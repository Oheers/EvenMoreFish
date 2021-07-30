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
        }
    }

    public void begin() {
        active = true;
        this.statusBar = new Bar();
        initTimer();
    }

    public void end() {
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

    public static String getLeaderboard() {
        if (active) {
            if (leaderboard.size() != 0) {
                StringBuilder builder = new StringBuilder();
                int pos = 0;
                leaderboard.resetLeaderboard();
                for (Node node : leaderboard.getTopEntrants(leaderboard.root, EvenMoreFish.msgs.getLeaderboardCount())) {
                    System.out.println("node 1: " + node.getLength());
                    System.out.println("node 2: " + node.getFisher());
                    System.out.println("node 3: " + node.getFish().getName());
                    System.out.println("thing 1: " + leaderboard.topEntrants.size());
                    System.out.println("thing 2: " + leaderboard.playerRegister.size());
                    pos++;
                    Fish fish = node.getFish();
                    builder.append(new Message()
                            .setPositionColour(EvenMoreFish.msgs.getPosColour(pos))
                            .setPosition(Integer.toString(pos))
                            .setRarity(fish.getRarity().getValue())
                            .setFishCaught(fish.getName())
                            .setPlayer(node.getFisher().getName())
                            .setLength(Float.toString(node.getLength()))
                            .setMSG(EvenMoreFish.msgs.getLeaderboard())
                            .toString());
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
}
