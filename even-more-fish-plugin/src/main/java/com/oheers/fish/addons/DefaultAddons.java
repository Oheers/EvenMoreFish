package com.oheers.fish.addons;

public enum DefaultAddons {
    J9("9"),
    J16("16"),
    J17("17")
    ;

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
