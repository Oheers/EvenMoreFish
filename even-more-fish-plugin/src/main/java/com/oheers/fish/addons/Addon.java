package com.oheers.fish.addons;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.bukkit.Bukkit;


public interface Addon {

    /**
     * @return The prefix used by the addon: "playerhead:id" or "base64:id" or "prefix:id"
     */
    String getPrefix();

    /**
     * @return The dependant plugin name, if there isn't one, you can set this to null.
     * You should override {@link #canRegister() canRegister() method} if there is no dependant plugin.
     */
    String getPluginName();


    /**
     * Can this addon be registered.
     */
    default boolean canRegister() {
        return (Bukkit.getPluginManager().getPlugin(getPluginName()) != null) && SystemUtils.isJavaVersionAtLeast(getRequiredJavaVersion());
    }

    /**
     * @return The author's name
     */
    String getAuthor();

    default JavaVersion getRequiredJavaVersion() {
        return JavaVersion.JAVA_1_8;
    }

}
