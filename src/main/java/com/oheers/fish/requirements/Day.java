package com.oheers.fish.requirements;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class Day implements Requirement {

    private final String configLocation;
    public final FileConfiguration fileConfig;
    private int day = 0;
    private final Calendar calendar = Calendar.getInstance();

    /**
     * Used during the Christmas 2022 advent calendar event to limit certain fish to certain days. A check is done to
     * make sure that A) it's December 2022 and B) it's the right day.
     *
     * @param configLocation The location that data regarding this should be found. This is different to other requirements
     *                       as it is found on the same line as the "requirements" and "glow: true" line.
     * @param fileConfig The file configuration to fetch file data from, for this specific requirement it should always
     *                   be the now-unused xmas2022.yml file.
     */
    public Day(@NotNull final String configLocation, @NotNull final FileConfiguration fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        return (calendar.get(Calendar.DATE) == this.day) && (calendar.get(Calendar.MONTH) == Calendar.DECEMBER);
    }

    @Override
    public void fetchData() {
        this.day = fileConfig.getInt(this.configLocation);
    }
}
