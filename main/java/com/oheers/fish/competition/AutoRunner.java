package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;

public class AutoRunner {

    static String timeKey;

    public static void init() {
        new BukkitRunnable(){
            public void run(){
                // creates a key similar to the time key given in config.yml
                timeKey = String.format("%02d", LocalTime.now().getHour()) + ":" + String.format("%02d", LocalTime.now().getMinute());

                // Obtaining how many minutes have passed since midnight last Sunday
                String day = LocalDate.now().getDayOfWeek().toString();
                int weekMinute = EvenMoreFish.competitionQueue.generateTimeCode(day, timeKey);

                // Beginning the competition set for schedule
                if (EvenMoreFish.competitionQueue.competitions.containsKey(weekMinute)) {
                    EvenMoreFish.active = EvenMoreFish.competitionQueue.competitions.get(weekMinute);
                    EvenMoreFish.active.begin();
                }
            }

            // delay is set to start at bang on :00 of every minute, period is set to run once a minute
        }.runTaskTimer(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class), (60-LocalTime.now().getSecond())*20, 20*60);// (60-LocalTime.now().getSecond())*20
    }
}
