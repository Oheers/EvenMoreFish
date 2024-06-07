package com.oheers.fish.api.addons.exceptions;


import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.JavaVersion;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown when there is a problem with the addon due to a mismatch in the required Java version.
 */
public class JavaVersionException extends Exception {
    private final JavaVersion requiredJavaVersion;

    /**
     * Constructs a new JavaVersionException with the specified plugin name and required Java version.
     *
     * @param pluginName          the name of the plugin
     * @param requiredJavaVersion the required Java version for the addon
     */
    public JavaVersionException(String pluginName, @NotNull JavaVersion requiredJavaVersion) {
        super(String.format("There is a problem with the addon for %s %nRequired jvm version is at least %s%n Running JVM version %s",
                pluginName, requiredJavaVersion, SystemUtils.JAVA_VERSION));
        this.requiredJavaVersion = requiredJavaVersion;
    }

    /**
     * Returns the required Java version for the addon.
     *
     * @return the required Java version
     */
    public JavaVersion getRequiredJavaVersion() {
        return requiredJavaVersion;
    }
}
