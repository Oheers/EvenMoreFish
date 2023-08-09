package com.oheers.fish.requirements;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RequirementContext {

    World world;
    Location location;
    Player player;

    /**
     * Provides data about the current server to the requirement checker, for example it passes through the current
     * tick time of the day so the in-game time limit can be compared, or the location the player is in so the world
     * can be checked or the regions or y-level can be checked.
     */
    public RequirementContext() {
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location variable as well as the world variable, the world variable is fetched from the #getWorld() from
     * Location.
     *
     * @param location The location.
     */
    public void setLocation(Location location) {
        this.location = location;
        this.world = location.getWorld();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
