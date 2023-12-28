package com.oheers.fish.api.addons.exceptions;



public class RequiredPluginException extends Exception {
    private final String requiredPlugin;

    public RequiredPluginException(String requiredPlugin) {
        super(String.format("Required plugin %s is not installed ", requiredPlugin));
        this.requiredPlugin = requiredPlugin;
    }

    public String getRequiredPlugin() {
        return requiredPlugin;
    }
}
