package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompetitionQueue {

    Map<Integer, Competition> competitions;
    List<String> days = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    public void load() {
        competitions = new HashMap<>();
        // Competitions exist in the competitions.yml
        if (EvenMoreFish.competitionConfig.getCompetitions() != null) {
            for (String comp : EvenMoreFish.competitionConfig.getCompetitions()) {

                CompetitionType type = EvenMoreFish.competitionConfig.getCompetitionType(comp);
                Competition competition = new Competition(EvenMoreFish.competitionConfig.getCompetitionDuration(comp)*60, type);

                if (type == CompetitionType.SPECIFIC_FISH) competition.chooseFish(comp, false);
                else competition.leaderboardApplicable = true;

                competition.initAlerts(comp);
                competition.initRewards(comp, false);
                competition.initBar(comp);
                competition.initGetNumbersNeeded(comp);

                if (EvenMoreFish.competitionConfig.specificDayTimes(comp)) {
                    for (String day : EvenMoreFish.competitionConfig.activeDays(comp)) {
                        for (String time : EvenMoreFish.competitionConfig.getDayTimes(comp, day)) {
                            competitions.put(generateTimeCode(day, time), competition);
                        }
                    }
                } else if (EvenMoreFish.competitionConfig.doingRepeatedTiming(comp)) {
                    if (EvenMoreFish.competitionConfig.hasBlacklistedDays(comp)) {
                        List<String> blacklistedDays = EvenMoreFish.competitionConfig.getBlacklistedDays(comp);
                        for (String time : EvenMoreFish.competitionConfig.getRepeatedTiming(comp)) {
                            for (String day : days) {
                                if (!blacklistedDays.contains(day)) {
                                    competitions.put(generateTimeCode(day, time), competition);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Converts "Wednesday, 14:30" for example, into the minute of the week, Wednesday 14:30 becomes (24*60*2) + (14*60) + 30
    // day = the day, tfh = 24h format, like 14:30 or 08:15
    public Integer generateTimeCode(String day, String tfh) {
        // Gets how many minutes have passed before midnight of the "day" variable
        int beginning = days.indexOf(day.toUpperCase()) * 24 * 60;

        if (tfh != null) {
            String[] time = tfh.split(":");
            // Time is formatted incorrectly
            if (time.length != 2) return -1;

            try {
                beginning += Integer.parseInt(time[0]) * 60;
                beginning += Integer.parseInt(time[1]);
            } catch (NumberFormatException e) {
                // The config contains a non-int value for the time
                return -1;
            }
        }

        return beginning;
    }
}
