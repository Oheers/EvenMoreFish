package com.oheers.fish.api.addons.exceptions;


import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.JavaVersion;
import org.jetbrains.annotations.NotNull;

public class JavaVersionException extends Exception {
    private final JavaVersion requiredJavaVersion;

    public JavaVersionException(String pluginName, @NotNull JavaVersion requiredJavaVersion) {
        super(String.format("There is a problem with the addon for %s %nRequired jvm version is at least %s%n Running JVM version %s",pluginName, requiredJavaVersion, SystemUtils.JAVA_VERSION));
        this.requiredJavaVersion = requiredJavaVersion;
    }

    public JavaVersion getRequiredJavaVersion() {
        return requiredJavaVersion;
    }
}
