package com.ibexmc.bazaar.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Particles {
    /**
     * Displays particles for denying access
     * @param location Location to display particles
     * @param player Display for which player
     */
    public static void denyAccess(Location location, Player player) {
        float volume = (float) 0.5;
        Particle particle;
        if (SpigotVersion.isVersionSafe(1.21)) {
            particle = Particle.valueOf("ANGRY_VILLAGER");
        } else {
            particle = Particle.valueOf("VILLAGER_ANGRY");
        }
        if (particle == null) {
            particle = Particle.FLAME;
        }
        player.getWorld().playSound(location, Sound.ENTITY_VILLAGER_NO, volume, 1);
        player.getWorld().spawnParticle(particle, location, 5, -1, 1.5, -1, 3);
    }
}
