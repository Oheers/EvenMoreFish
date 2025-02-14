package com.oheers.fish.api.requirement;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class RequirementContext {
    WeakReference<World> worldRef;
    Location location;
    Player player;
    YamlDocument config;
    String configPath;

    /**
     * Provides data about the current server to the requirement checker, for example it passes through the current
     * tick time of the day so the in-game time limit can be compared, or the location the player is in so the world
     * can be checked or the regions or y-level can be checked.
     */
    public RequirementContext(@Nullable World world, @Nullable Location location, @Nullable Player player, @Nullable YamlDocument config, @Nullable String configPath) {
        this.worldRef = new WeakReference<>(world);
        this.location = location;
        this.player = player;
        this.config = config;
        this.configPath = configPath;
    }

    public @Nullable World getWorld() {
        return worldRef.get();
    }

    public void setWorld(World world) {
        this.worldRef = new WeakReference<>(world);
    }

    public @Nullable Location getLocation() {
        return location;
    }

    public void setConfig(YamlDocument config) {
        this.config = config;
    }

    public @Nullable YamlDocument getConfig() {
        return this.config;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public @Nullable String getConfigPath() {
        return this.configPath;
    }

    /**
     * Sets the location variable as well as the world variable, the world variable is fetched from the #getWorld() from
     * Location.
     *
     * @param location The location.
     */
    public void setLocation(Location location) {
        this.location = location;
        this.worldRef = new WeakReference<>(location.getWorld());
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
