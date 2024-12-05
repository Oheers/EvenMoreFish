package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.config.CompetitionConfig;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.time.DayOfWeek;
import java.util.*;

public class CompetitionQueue {

    Map<Integer, Competition> competitions;

    public void load() {
        competitions = new TreeMap<>();

        File compsFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "competitions");
        List<File> competitionFiles = FileUtil.getFilesInDirectory(compsFolder, true, true);

        if (competitionFiles.isEmpty()) {
            return;
        }

        competitionFiles.forEach(file -> {
            System.out.println("Loading " + file.getName());
            CompetitionFile competitionFile;
            try {
                competitionFile = new CompetitionFile(file);
            } catch (InvalidConfigurationException e) {
                return;
            }
            Competition competition = new Competition(competitionFile);
            System.out.println("Loading Timing for " + file.getName());
            loadRepeatedTiming(competition);
        });
    }

    private void loadRepeatedTiming(Competition competition) {
        CompetitionFile file = competition.getCompetitionFile();
        List<String> repeatedTimes = file.getTimes();

        // Get a list of days we can use.
        List<DayOfWeek> daysToUse = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        daysToUse.removeAll(file.getBlacklistedDays());

        for (String time : repeatedTimes) {
            for (DayOfWeek day : daysToUse) {
                competitions.put(generateTimeCode(day, time), competition);
            }
        }
    }

    // Converts "Wednesday, 14:30" for example, into the minute of the week, Wednesday 14:30 becomes (24*60*2) + (14*60) + 30
    // day = the day, tfh = 24h format, like 14:30 or 08:15
    public int generateTimeCode(DayOfWeek day, String tfh) {

        // Gets how many minutes have passed before midnight of the "day" variable
        int beginning = Arrays.asList(DayOfWeek.values()).indexOf(day) * 24 * 60;

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

        System.out.println("Time Code: " + beginning);

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

        Competition competition = new Competition(-1, CompetitionType.LARGEST_FISH);
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
