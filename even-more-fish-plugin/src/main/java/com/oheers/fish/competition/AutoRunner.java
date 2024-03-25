package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;

import java.time.LocalDate;
import java.time.LocalTime;

public class AutoRunner {

    static String timeKey;

    static int lastMinute;

    public static void init() {
        EvenMoreFish.getScheduler().runTaskTimer(() -> {
            // If the minute hasn't been checked against the competition queue.
            if (!wasMinuteChecked()) {
                int weekMinute = getCurrentTimeCode();

                // Beginning the competition set for schedule
                CompetitionQueue queue = EvenMoreFish.getInstance().getCompetitionQueue();
                if (queue.competitions.containsKey(weekMinute)) {
                    if (!Competition.isActive()) {
                        EvenMoreFish.getInstance().setActiveCompetition(queue.competitions.get(weekMinute));
                        EvenMoreFish.getInstance().getActiveCompetition().begin(false);
                    }
                }
            }
        }, (60 - LocalTime.now().getSecond()) * 20, 20);
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
        return EvenMoreFish.getInstance().getCompetitionQueue().generateTimeCode(day, timeKey);
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
