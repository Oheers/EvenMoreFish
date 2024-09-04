package com.oheers.fish.competition;

import com.oheers.fish.config.CompetitionConfig;

import java.util.*;

public class CompetitionQueue {

    Map<Integer, Competition> competitions;
    List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    public void load() {
        competitions = new TreeMap<>();
        CompetitionConfig competitionConfig = CompetitionConfig.getInstance();

        Set<String> configuredCompetitions = competitionConfig.getCompetitions();

        if (configuredCompetitions.isEmpty()) {
            return;
        }

        configuredCompetitions.forEach(comp -> {
            Competition competition = createCompetition(comp, competitionConfig);

            if (competitionConfig.specificDayTimes(comp)) {
                loadSpecificDayTimes(comp, competitionConfig, competition);
            } else if (competitionConfig.doingRepeatedTiming(comp)) {
                loadRepeatedTiming(comp, competitionConfig, competition);
            }
        });
    }

    private Competition createCompetition(String comp, CompetitionConfig competitionConfig) {
        CompetitionType type = competitionConfig.getCompetitionType(comp);
        Competition competition = new Competition(
                competitionConfig.getCompetitionDuration(comp) * 60,
                type,
                competitionConfig.getCompetitionStartCommands(comp)
        );

        competition.setCompetitionName(comp);
        competition.setAdminStarted(false);
        competition.initAlerts(comp);
        competition.initRewards(comp, false);
        competition.initBar(comp);
        competition.initGetNumbersNeeded(comp);
        competition.initStartSound(comp);

        return competition;
    }

    private void loadSpecificDayTimes(String comp, CompetitionConfig competitionConfig, Competition competition) {
        for (String day : competitionConfig.activeDays(comp)) {
            for (String time : competitionConfig.getDayTimes(comp, day)) {
                competitions.put(generateTimeCode(day, time), competition);
            }
        }
    }

    private void loadRepeatedTiming(String comp, CompetitionConfig competitionConfig, Competition competition) {
        List<String> repeatedTimes = competitionConfig.getRepeatedTiming(comp);
        List<String> daysToUse = new ArrayList<>(days);

        if (competitionConfig.hasBlacklistedDays(comp)) {
            daysToUse.removeAll(competitionConfig.getBlacklistedDays(comp));
        }

        for (String time : repeatedTimes) {
            for (String day : daysToUse) {
                competitions.put(generateTimeCode(day, time), competition);
            }
        }
    }

    // Converts "Wednesday, 14:30" for example, into the minute of the week, Wednesday 14:30 becomes (24*60*2) + (14*60) + 30
    // day = the day, tfh = 24h format, like 14:30 or 08:15
    public int generateTimeCode(String day, String tfh) {
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
        int currentTimeCode = AutoRunner.getCurrentTimeCode();

        if (competitions.containsKey(currentTimeCode)) {
            return currentTimeCode;
        }

        Competition competition = new Competition(-1, CompetitionType.LARGEST_FISH, new ArrayList<>());
        competitions.put(currentTimeCode, competition);

        int nextTimeCode = findNextCompetitionTimeCode(currentTimeCode);

        competitions.remove(currentTimeCode);
        return nextTimeCode;
    }

    private int findNextCompetitionTimeCode(int currentTimeCode) {
        List<Integer> timeCodes = new ArrayList<>(competitions.keySet());
        int position = timeCodes.indexOf(currentTimeCode);

        if (position == competitions.size() - 1) {
            return timeCodes.get(0);
        } else {
            return timeCodes.get(position + 1);
        }
    }

}
