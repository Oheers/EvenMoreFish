package com.oheers.fish.requirements;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

public class Disabled implements Requirement {

    private String configLocation;
    public final YamlDocument fileConfig;
    private boolean isDisabled;

    /**
     * Lets server just completely disable this fish, the requirementMet will always return false if the fish has been
     * disabled, meaning it will never appear in fish random weight pools.
     *
     * @param configLocation The location that data regarding this should be found. This is different to other requirements
     *                       as it is found on the same line as the "requirements" and "glow: true" line.
     * @param fileConfig The file configuration to fetch file data from, this is either the rarities or fish.yml file,
     *                   but it would be possible to use any file, as long as the configLocation is correct.
     */
    public Disabled(String configLocation, @NotNull final YamlDocument fileConfig) {
        this.configLocation = configLocation;
        this.fileConfig = fileConfig;
        fetchData();
    }

    @Override
    public boolean requirementMet(RequirementContext context) {
        return !isDisabled;
    }

    @Override
    public void fetchData() {
        this.isDisabled = fileConfig.getBoolean(configLocation);
    }
}
