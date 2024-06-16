package com.oheers.fish.api.addons.exceptions;


/**
 * This exception is thrown when an incorrect assigned material is encountered.
 * It provides information about the configuration location and the ID of the incorrect material.
 */
public class IncorrectAssignedMaterialException extends Exception {
    private final String id;
    private final String configLocation;

    /**
     * Constructs a new IncorrectAssignedMaterialException with the specified configuration location and ID.
     *
     * @param configLocation the location of the configuration file where the incorrect material is assigned
     * @param id             the ID of the incorrect material
     */
    public IncorrectAssignedMaterialException(String configLocation, String id) {
        super(String.format("%s has an incorrect assigned material: %s", configLocation, id));
        this.id = id;
        this.configLocation = configLocation;
    }

    /**
     * Returns the ID of the incorrect material.
     *
     * @return the ID of the incorrect material
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the location of the configuration file where the incorrect material is assigned.
     *
     * @return the location of the configuration file
     */
    public String getConfigLocation() {
        return configLocation;
    }
}
