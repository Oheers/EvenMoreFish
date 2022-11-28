package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class Day implements Requirement {

    private final String configLocation;
    private int day = 0;
    private final Calendar calendar = Calendar.getInstance();

    /**
     * Used during the Christmas 2022 advent calendar event to limit certain fish to certain days. A check is done to
     * make sure that A) it's December 2022 and B) it's the right day.
     *
     * @param configLocation The location that data regarding this should be found. This is different to other requirements
     *                       as it is found on the same line as the "requirements" and "glow: true" line.
     */
    public Day(@NotNull final String configLocation) {
        this.configLocation = configLocation;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        return (calendar.get(Calendar.DATE) == this.day) && (calendar.get(Calendar.MONTH) == Calendar.NOVEMBER);
    }

    @Override
    public void fetchData() {
        this.day = EvenMoreFish.xmas2022Config.getConfig().getInt(this.configLocation);
    }
}
