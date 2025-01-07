package com.oheers.fish.addons;

public enum DefaultAddons {
    J17("17"),
    J21("21");

    private final String targetJavaVersion;
    private final String fullFileName;

    DefaultAddons(String targetJavaVersion) {
        this.targetJavaVersion = targetJavaVersion;
        this.fullFileName = "EMF-Addons-J" + targetJavaVersion + ".addon";
    }

    public String getTargetJavaVersion() {
        return targetJavaVersion;
    }

    public String getFullFileName() {
        return fullFileName;
    }
}
