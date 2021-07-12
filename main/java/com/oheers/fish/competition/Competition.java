package com.oheers.fish.competition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.SortedMap;

public class Competition {

    Integer maxDuration, timeLeft;
    CompetitionType competitionType;

    boolean active;

    // In a SPECIFIC_FISH competition, there won't be a leaderboard
    boolean leaderboardApplicable;
    public static SortedMap<CompetitionEntry, Player> leaderboard;

    public Competition(final Integer duration, final CompetitionType type) {
        this.maxDuration = duration;
        this.timeLeft = duration;
        this.competitionType = type;

        if (type != CompetitionType.SPECIFIC_FISH) {
            leaderboardApplicable = true;
        }
    }

    public void begin() {
        this.active = true;
        initTimer();
    }

    public void end() {
        this.active = false;
    }

    public static String getLeaderboard() {
        return "leaderboard";
    }

    // Starts an async task to decrease the time left by 1s each second
    private void initTimer() {
        new BukkitRunnable() {

            @Override
            public void run() {
                timeLeft--;

                if (timeLeft == 300) {
                    System.out.println("5m left lads come on chop chop.");
                } else if (timeLeft == 0) {
                    end();
                }
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("EvenMoreFish")), 0, 20);
    }
}
