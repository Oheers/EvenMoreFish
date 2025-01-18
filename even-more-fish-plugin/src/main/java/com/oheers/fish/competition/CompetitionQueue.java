package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.competition.configs.CompetitionConversions;
import com.oheers.fish.competition.configs.CompetitionFile;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.DayOfWeek;
import java.util.*;
import java.util.logging.Level;

public class CompetitionQueue {

    Map<Integer, Competition> competitions;
    TreeMap<String, CompetitionFile> fileMap;

    public CompetitionQueue() {
        competitions = new TreeMap<>();
        fileMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Check for old file format and convert if it exists.
        new CompetitionConversions().performCheck();
    }

    public void load() {
        competitions.clear();
        fileMap.clear();

        File compsFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "competitions");
        if (EvenMoreFish.getInstance().isFirstLoad()) {
            loadDefaultFiles(compsFolder);
        }
        regenExampleFile(compsFolder);
        List<File> competitionFiles = FileUtil.getFilesInDirectory(compsFolder, true, true);

        if (competitionFiles.isEmpty()) {
            return;
        }

        competitionFiles.forEach(file -> {
            EvenMoreFish.debug("Loading " + file.getName() + " competition");
            CompetitionFile competitionFile;
            try {
                competitionFile = new CompetitionFile(file);
            // Skip invalid configs.
            } catch (InvalidConfigurationException e) {
                return;
            }
            // Skip disabled files.
            if (competitionFile.isDisabled()) {
                return;
            }
            // Skip duplicate IDs
            if (fileMap.containsKey(competitionFile.getId())) {
                EvenMoreFish.getInstance().getLogger().warning("A competition with the id: " + competitionFile.getId() + " already exists! Skipping.");
                return;
            }
            fileMap.put(competitionFile.getId(), competitionFile);
            Competition competition = new Competition(competitionFile);
            if (loadSpecificDayTimes(competition)) {
                return;
            }
            if (loadRepeatedTiming(competition)) {
                return;
            }
            EvenMoreFish.debug(Level.WARNING, file.getName() + "'s timings are not configured properly. This competition will never automatically start.");
        });
    }

    private void regenExampleFile(@NotNull File targetDirectory) {
        new File(targetDirectory, "_example.yml").delete();
        FileUtil.loadFileOrResource(targetDirectory, "_example.yml", "competitions/_example.yml", EvenMoreFish.getInstance());
    }

    private void loadDefaultFiles(@NotNull File targetDirectory) {
        EvenMoreFish.getInstance().getLogger().info("Loading default competition configs.");
        FileUtil.loadFileOrResource(targetDirectory, "main.yml", "competitions/main.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "sunday1.yml", "competitions/sunday1.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "sunday2.yml", "competitions/sunday2.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "weekend.yml", "competitions/weekend.yml", EvenMoreFish.getInstance());
    }

    public TreeMap<String, CompetitionFile> getFileMap() {
        return new TreeMap<>(fileMap);
    }

    private boolean loadSpecificDayTimes(@NotNull Competition competition) {
        Map<DayOfWeek, List<String>> scheduledDays = competition.getCompetitionFile().getScheduledDays();
        if (scheduledDays.isEmpty()) {
            return false;
        }
        scheduledDays.forEach((day, times) ->
                times.forEach(time ->
                        competitions.put(generateTimeCode(day, time), competition)
                )
        );
        return true;
    }

    private boolean loadRepeatedTiming(@NotNull Competition competition) {
        CompetitionFile file = competition.getCompetitionFile();
        List<String> repeatedTimes = file.getTimes();

        if (repeatedTimes.isEmpty()) {
            return false;
        }

        // Get a list of days we can use.
        List<DayOfWeek> daysToUse = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        daysToUse.removeAll(file.getBlacklistedDays());

        for (String time : repeatedTimes) {
            for (DayOfWeek day : daysToUse) {
                competitions.put(generateTimeCode(day, time), competition);
            }
        }
        return true;
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
