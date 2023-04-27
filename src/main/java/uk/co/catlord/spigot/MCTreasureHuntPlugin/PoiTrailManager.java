package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.ParticleTrailUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureCompassUtils;

public class PoiTrailManager implements Listener {
  public static void register(JavaPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(new PoiTrailManager(), plugin);
  }

  @EventHandler
  public void onUseCompass(PlayerInteractEvent event) {
    // Exit early if the player isn't holding a treasure compass
    if (!TreasureCompassUtils.isTreasureCompassItemStack(event.getItem())) {
      return;
    }

    Location start = PlayerUtils.getLocationInfrontOfPlayer(event.getPlayer(), 1).add(0, 1, 0);
    Location poiLocation = event.getPlayer().getLocation().add(20, 1, 10);
    ParticleTrailUtils.createParticleTrail(start, poiLocation, 10, Color.PURPLE);

    event.setCancelled(true);
  }
}
