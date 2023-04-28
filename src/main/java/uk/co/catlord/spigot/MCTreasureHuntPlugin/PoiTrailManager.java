package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.Checkpoint;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
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

    List<Checkpoint> checkpoints = getTreasureBeaconCheckpointsForPlayer(event.getPlayer());

    if (checkpoints.size() == 0) {
      event
          .getPlayer()
          .sendMessage(
              ChatColor.GRAY
                  + "You already visited all the treasure beacons for this checkpoint. Follow your"
                  + " compass to the next checkpoint.");
      return;
    }

    int delay = 0;
    for (Checkpoint checkpoint : checkpoints) {
      Location poiLocation = checkpoint.shape.getCenter();

      Bukkit.getScheduler()
          .runTaskLater(
              App.instance,
              () -> {
                Color color = checkpoint.color;
                if (color == null) {
                  color = Color.WHITE;
                }
                ParticleTrailUtils.createParticleTrail(start, poiLocation, 10, color);
              },
              delay);

      delay += 50;
    }

    event.setCancelled(true);
  }

  private List<Checkpoint> getTreasureBeaconCheckpointsForPlayer(Player player) {
    List<Checkpoint> checkpoints = new ArrayList<>();
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);

    if (playerDataResult.isError()) {
      return checkpoints;
    }

    PlayerData playerData = playerDataResult.getValue();

    for (Checkpoint checkpoint : CheckpointDataStore.getStore().checkpoints) {
      if (!checkpoint.isTreasureBeacon()) {
        continue;
      }
      if (checkpoint.trailFrom == null) {
        continue;
      }
      if (!checkpoint.trailFrom.equals(playerData.getCurrentCheckpointName())) {
        continue;
      }
      if (playerData.hasVisitedCheckpoint(checkpoint.name)) {
        continue;
      }
      checkpoints.add(checkpoint);
    }

    return checkpoints;
  }
}
