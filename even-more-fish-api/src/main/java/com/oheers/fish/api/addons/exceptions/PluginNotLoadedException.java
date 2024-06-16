package com.oheers.fish.api.addons.exceptions;


/**
 * Custom exception class for when a plugin has not been loaded.
 */
public class PluginNotLoadedException extends Exception {

    /** The name of the plugin that has not been loaded. */
    private final String pluginName;

    /**
     * Constructs a PluginNotLoadedException with the specified plugin name.
     *
     * @param pluginName the name of the plugin that has not been loaded
     */
    public PluginNotLoadedException(String pluginName) {
        super(String.format("%s has not loaded yet.", pluginName));
        this.pluginName = pluginName;
    }

    /**
     * Gets the name of the plugin associated with this exception.
     *
     * @return the name of the plugin
     */
    public String getPluginName() {
        return pluginName;
    }
}