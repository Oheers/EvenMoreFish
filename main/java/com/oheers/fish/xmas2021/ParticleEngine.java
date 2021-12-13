package com.oheers.fish.xmas2021;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.FishHook;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ParticleEngine {

    static final Particle.DustTransition red = new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(255, 128, 128), 1.0F);
    static final Particle.DustTransition green = new Particle.DustTransition(Color.fromRGB(0, 255, 0), Color.fromRGB(128, 255, 128), 1.0F);
    static final Particle.DustTransition white = new Particle.DustTransition(Color.WHITE, Color.WHITE, 1.0F);

    static final List<Particle.DustTransition> colours = Arrays.asList(red, green, white);

    /*
     * This is for the chrismtas 2021 event and is therefore a work in progress. Limited functionality currently exists
     */

    public static void renderParticles(FishHook hook) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hook.isDead()) this.cancel();

                if (hook.isInWater()) {
                    Location spawnLocation = hook.getLocation();
                    spawnLocation.setX(spawnLocation.getX() + ThreadLocalRandom.current().nextFloat() * 1.5f - 0.75f);
                    spawnLocation.setY(spawnLocation.getY() + ThreadLocalRandom.current().nextFloat() * 1.5f - 0.75f);
                    spawnLocation.setZ(spawnLocation.getZ() + ThreadLocalRandom.current().nextFloat() * 1.5f - 0.75f);
                    hook.getLocation().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, spawnLocation, 1, colours.get(ThreadLocalRandom.current().nextInt(0, 3)));
                }
            }
        }.runTaskTimer(JavaPlugin.getProvidingPlugin(ParticleEngine.class), 0, 1);
    }

}
