package com.oheers.fish.requirements;

public interface Requirement {

    /**
     * @param context Data about the current state of the server.
     * @return Whether the current server conditions match the requirements set by the configuration.
     */
    boolean requirementMet(RequirementContext context);

    /**
     * Fetches all necessary data from the config file specified by the configLocation variable. During a reload,
     * this must be done after the fish-file has been reloaded to prevent old data being used still.
     */
    void fetchData();
}
