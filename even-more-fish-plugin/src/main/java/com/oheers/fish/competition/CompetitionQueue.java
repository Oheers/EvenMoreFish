package com.oheers.fish.competition;

import com.oheers.fish.config.CompetitionConfig;

import java.util.*;

public class CompetitionQueue {

    Map<Integer, Competition> competitions;
    List<String> days = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    public void load() {
        competitions = new TreeMap<>();
        CompetitionConfig competitionConfig = CompetitionConfig.getInstance();
        // Competitions exist in the competitions.yml
        if (competitionConfig.getCompetitions() != null) {
            for (String comp : competitionConfig.getCompetitions()) {

                CompetitionType type = competitionConfig.getCompetitionType(comp);
                Competition competition = new Competition(competitionConfig.getCompetitionDuration(comp) * 60, type, competitionConfig.getCompetitionStartCommands(comp));

                competition.setCompetitionName(comp);
                competition.setAdminStarted(false);
                competition.initAlerts(comp);
                competition.initRewards(comp, false);
                competition.initBar(comp);
                competition.initGetNumbersNeeded(comp);
                competition.initStartSound(comp);

                if (competitionConfig.specificDayTimes(comp)) {
                    for (String day : competitionConfig.activeDays(comp)) {
                        for (String time : competitionConfig.getDayTimes(comp, day)) {
                            competitions.put(generateTimeCode(day, time), competition);
                        }
                    }
                } else if (competitionConfig.doingRepeatedTiming(comp)) {
                    if (competitionConfig.hasBlacklistedDays(comp)) {
                        List<String> blacklistedDays = competitionConfig.getBlacklistedDays(comp);
                        for (String time : competitionConfig.getRepeatedTiming(comp)) {
                            for (String day : days) {
                                if (!blacklistedDays.contains(day)) {
                                    competitions.put(generateTimeCode(day, time), competition);
                                }
                            }
                        }
                    } else {
                        for (String time : competitionConfig.getRepeatedTiming(comp)) {
                            for (String day : days) {
                                competitions.put(generateTimeCode(day, time), competition);
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

    /**
     * @return The number of competitions in the competition queue.
     */
    public int getSize() {
        return competitions.size();
    }

    /**
     * Puts a test competition into the competition queue and figures out the location of the test competition. If there's
     * values after this, the next one's time is returned, otherwise the first competition's time is returned.
     *
     * @return The next competition starting timecode.
     */
    public int getNextCompetition() {
        Competition competition = new Competition(-1, CompetitionType.LARGEST_FISH, new ArrayList<>());
        int currentTimeCode = AutoRunner.getCurrentTimeCode();
        if (this.competitions.containsKey(currentTimeCode)) return currentTimeCode;
        this.competitions.put(currentTimeCode, competition);
        int position = new ArrayList<>(this.competitions.keySet()).indexOf(currentTimeCode);
        if (position == this.competitions.size() - 1) {
            this.competitions.remove(currentTimeCode);
            return this.competitions.keySet().iterator().next();
        } else {
            int i = 0;
            for (Map.Entry<Integer, Competition> integerCompetitionEntry : this.competitions.entrySet()) {
                if (i == position + 1) {
                    this.competitions.remove(currentTimeCode);
                    return integerCompetitionEntry.getKey();
                }
                i++;
            }
            this.competitions.remove(currentTimeCode);
            return -1;
        }
    }
}
