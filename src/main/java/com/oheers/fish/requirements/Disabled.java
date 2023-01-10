package com.oheers.fish.requirements;

import com.oheers.fish.EvenMoreFish;

public class Disabled implements Requirement {

    private String configLocation;
    private boolean isDisabled;

    /**
     * Lets server just completely disable this fish, the requirementMet will always return false if the fish has been
     * disabled, meaning it will never appear in fish random weight pools.
     *
     * @param configLocation The location that data regarding this should be found. This is different to other requirements
     *                       as it is found on the same line as the "requirements" and "glow: true" line.
     */
    public Disabled(String configLocation) {
        this.configLocation = configLocation;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        return !isDisabled;
    }

    @Override
    public void fetchData() {
        this.isDisabled = EvenMoreFish.fishFile.getConfig().getBoolean(configLocation);
    }
}
