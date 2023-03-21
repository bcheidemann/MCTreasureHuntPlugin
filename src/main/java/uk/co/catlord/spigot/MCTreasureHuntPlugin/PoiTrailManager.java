package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.ParticleTrailUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;

public class PoiTrailManager implements Listener {
  public static void register(JavaPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(new PoiTrailManager(), plugin);
  }

  @EventHandler
  public void onToggleSneak(PlayerToggleSneakEvent event) {
    if (event.isSneaking()) {
      Location start = PlayerUtils.getLocationInfrontOfPlayer(event.getPlayer(), 1).add(0, 1, 0);
      Location poiLocation = event.getPlayer().getLocation().add(20, 1, 10);
      ParticleTrailUtils.createParticleTrail(start, poiLocation, 10, Color.BLACK);
    }
  }
}
