package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;

import java.util.Map;

public class CompetitionQueue {

    Map<Integer, Competition> competitions;

    public void load() {
        // Competitions exist in the competitions.yml
        if (EvenMoreFish.competitionConfig.getCompetitions() != null) {
            for (String comp : EvenMoreFish.competitionConfig.getCompetitions()) {
                if (EvenMoreFish.competitionConfig.specificDayTimes(comp)) {

                    Competition competition = new Competition(
                            EvenMoreFish.competitionConfig.getCompetitionDuration(comp),
                            EvenMoreFish.competitionConfig.getCompetitionType(comp)
                    );

                    for (String day : EvenMoreFish.competitionConfig.activeDays(comp)) {
                        for (String time : EvenMoreFish.competitionConfig.getDayTimes(comp, day)) {
                            competitions.put(generateTimeCode(day, time), competition);
                        }
                    }
                }
            }
        }
    }

    // Converts "Wednesday, 14:30" for example, into the minute of the week, Wednesday 14:30 becomes (24*60*2) + (14*60) + 30
    // day = the day, tfh = 24h format, like 14:30 or 08:15
    private Integer generateTimeCode(String day, String tfh) {
        int beginning;
        switch (day.toUpperCase()) {
            case "TUESDAY": beginning = 60*24; break;
            case "WEDNESDAY": beginning = 60*24*2; break;
            case "THURSDAY": beginning = 60*24*3; break;
            case "FRIDAY": beginning = 60*24*4; break;
            case "SATURDAY": beginning = 60*24*5; break;
            case "SUNDAY": beginning = 60*24*6; break;
            default: beginning = 0; // default/monday
        }

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

        return beginning;
    }
}
