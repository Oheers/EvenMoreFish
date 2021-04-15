package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;

public class AutoRunner {

    static String timeKey;

    public static void init() {
        new BukkitRunnable(){
            public void run(){
                // creates a key similar to the time key given in config.yml
                timeKey = String.format("%02d", LocalTime.now().getHour()) + ":" + String.format("%02d", LocalTime.now().getMinute());
                // if that time key fits into the metaphoric lock that is the arrayList "competitionTimes"
                if (EvenMoreFish.mainConfig.getCompetitionTimes().contains(timeKey)) {
                    // if there isn't a competition going on
                    if (EvenMoreFish.active == null) {
                        Competition comp = new Competition(EvenMoreFish.mainConfig.getCompetitionDuration()*60);
                        comp.start(false);
                    }
                }
            }
            // delay is set to start at bang on :00 of every minute, period is set to run once a minute
        }.runTaskTimer(EvenMoreFish.getProvidingPlugin(EvenMoreFish.class), (60-LocalTime.now().getSecond())*20, 20*60);
    }
}
