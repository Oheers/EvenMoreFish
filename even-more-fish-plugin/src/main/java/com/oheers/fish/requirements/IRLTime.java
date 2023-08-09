package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.logging.Level;

public class IRLTime implements Requirement {

    public final String configLocation;
    public final FileConfiguration fileConfig;
    public int minTime, maxTime;

    /**
     * Compares the real-world time to make fish only available to be caught at certain times of the day. If minTime
     * is not set, the default value of 0 is used, if maxTime is not set, the default value of 1440 is used (24*60).
     * The time must be formatted in HH:MM format and will represent each day. The minTime and maxTime will be
     * converted to integer format to show the current minute of the day: for example, 02:30 would be 2(60) + 30.
     *
     * @param configLocation The location that data regarding this should be found. It should cut off after "irl-time:"
     *                       for example, "fish.Common.Herring.requirements.irl-time".
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public IRLTime(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        long currentTime = (Instant.now().getEpochSecond() / 60) % 1440 + 60;
        return currentTime >= minTime && currentTime < maxTime;
    }

    public void fetchData() {
        this.minTime = getDayMinute(fileConfig.getString(configLocation + ".minTime", "00:00"), 0);
        this.maxTime = getDayMinute(fileConfig.getString(configLocation + ".maxTime", "24:00"), 1440);
    }

    /**
     * Splits the HHMM format by ":" and multiplies the first element by 60 before adding it to the second element.
     *
     * @param HHMMFormat The time in the format of HH:MM.
     * @param fallback   The number to be used in case the HHMM format has not been followed correctly and the minute could
     *                   not be fetched.
     * @return The number of minutes that will have passed in the day by the time the clock ticks round to match the HHMM
     * format. If the HHMM format is incorrect however, the fallback will be returned.
     */
    public int getDayMinute(@NotNull final String HHMMFormat, final int fallback) {
        String[] time = HHMMFormat.split(":");
        try {
            return (Integer.parseInt(time[0]) * 60) + Integer.parseInt(time[1]);
        } catch (IndexOutOfBoundsException | NumberFormatException exception) {
            EvenMoreFish.logger.log(Level.SEVERE, "FATAL error reading " + HHMMFormat + ", resorting to default value of " + fallback);
            return fallback;
        }
    }
}
