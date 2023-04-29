package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.Checkpoint;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureCompassUtils;

public class PlayerRespawnManager implements Listener {
  public static void register(JavaPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(new PlayerRespawnManager(), plugin);
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    PlayerUtils.givePlayerItemStack(player, TreasureCompassUtils.createTreasureCompassItemStack());
    Bukkit.getScheduler()
        .runTaskLater(
            App.instance, () -> CheckpointDataStore.getStore().updateCompassForPlayer(player), 1);
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);
    if (playerDataResult.isError()) {
      player.sendMessage(
          ChatColor.RED
              + "Failed to get player data on respawn (please report to admin): "
              + playerDataResult.getError());
      return;
    }
    PlayerData playerData = playerDataResult.getValue();
    if (playerData.respawnPoint == null) {
      Checkpoint startCheckpoint = CheckpointDataStore.getStore().getCheckpointByName("START");

      if (startCheckpoint == null) {
        player.sendMessage(
            ChatColor.RED + "Failed to find start checkpoint (please report to admin)");
        return;
      }

      if (startCheckpoint.respawnPoint == null) {
        event.setRespawnLocation(startCheckpoint.shape.getCenter());
      }

      event.setRespawnLocation(startCheckpoint.respawnPoint);

      return;
    }
    event.setRespawnLocation(playerData.respawnPoint);
  }
}
