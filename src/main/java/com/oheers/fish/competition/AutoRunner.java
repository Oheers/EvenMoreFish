package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;

public class AutoRunner {

    static String timeKey;

    static int lastMinute;

    public static void init() {
        new BukkitRunnable() {
            public void run() {
                // If the minute hasn't been checked against the competition queue.
                if (!wasMinuteChecked()) {
                    int weekMinute = getCurrentTimeCode();

                    // Beginning the competition set for schedule
                    if (EvenMoreFish.competitionQueue.competitions.containsKey(weekMinute)) {
                        if (!Competition.isActive()) {
                            EvenMoreFish.active = EvenMoreFish.competitionQueue.competitions.get(weekMinute);
                            EvenMoreFish.active.begin(false);
                        }
                    }
                }
            }

            // delay is set to start at bang on :00 of every minute (assuming the tps to be 20), period is set to run once a minute
        }.runTaskTimer(
                // You provide the plugin's main class - the one that inherits from JavaPlugin
                EvenMoreFish.getProvidingPlugin(EvenMoreFish.class),
                // how long between this code first running and the first iteration you want (in ticks)
                (60 - LocalTime.now().getSecond()) * 20,
                // time between each loop (in ticks)
                20);
    }

    /**
     * Feeds through the current timekey and day name to the generateTimeCode method in the competition queue.
     *
     * @return The integer timecode for the current minute.
     */
    public static int getCurrentTimeCode() {
        // creates a key similar to the time key given in config.yml
        timeKey = String.format("%02d", LocalTime.now().getHour()) + ":" + String.format("%02d", LocalTime.now().getMinute());

        // Obtaining how many minutes have passed since midnight last Sunday
        String day = LocalDate.now().getDayOfWeek().toString();
        return EvenMoreFish.competitionQueue.generateTimeCode(day, timeKey);
    }

    /**
     * Uses the last minute to work out whether the plugin should run calculations for this minute or not, it also
     * automatically sets the lastMinute to the current minute if returning true.
     *
     * @return Whether minute checks need running to determine whether a competition needs to start.
     */
    private static boolean wasMinuteChecked() {
        if (lastMinute != LocalTime.now().getMinute()) {
            lastMinute = LocalTime.now().getMinute();
            return false;
        }

        return true;
    }
}
