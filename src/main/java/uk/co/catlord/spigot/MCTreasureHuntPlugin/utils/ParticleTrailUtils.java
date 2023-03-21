package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;

public class ParticleTrailUtils {
  public static void createParticleTrail(Location from, Location to, double length, Color color) {
    World world = from.getWorld();
    if (world == null || !world.equals(to.getWorld())) {
      throw new IllegalArgumentException("Both locations must be in the same world");
    }

    double deltaX = to.getX() - from.getX();
    double deltaY = to.getY() - from.getY();
    double deltaZ = to.getZ() - from.getZ();
    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

    // Normalize the direction vector
    double directionX = deltaX / distance;
    double directionY = deltaY / distance;
    double directionZ = deltaZ / distance;

    int startDelayTicks = 40;
    double stepSize = 0.2;
    int steps = (int) (length / stepSize);

    // Create a DustOptions instance with the provided color
    Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);

    double randomness = 0.2;
    double startX = from.getX();
    double startY = from.getY();
    double startZ = from.getZ();
    for (int i = 0; i <= startDelayTicks; i++) {
      final int j = i;
      Bukkit.getScheduler()
          .scheduleSyncDelayedTask(
              App.instance,
              () -> {
                double x = startX + (Math.random() - 0.5) * randomness;
                double y = startY + Math.sin((double) j / startDelayTicks * Math.PI * 4) * -0.25;
                double z = startZ + (Math.random() - 0.5) * randomness;
                Location particleLocation = new Location(world, x, y, z);
                world.spawnParticle(
                    Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, dustOptions);
              },
              i);
    }

    for (int i = 0; i <= steps; i++) {
      final int j = i;
      Bukkit.getScheduler()
          .scheduleSyncDelayedTask(
              App.instance,
              () -> {
                double x = from.getX() + directionX * stepSize * j;
                double y = from.getY() + directionY * stepSize * j;
                double z = from.getZ() + directionZ * stepSize * j;
                Location particleLocation = new Location(world, x, y, z);
                world.spawnParticle(
                    Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, dustOptions);
              },
              i + startDelayTicks);
    }
  }
}
