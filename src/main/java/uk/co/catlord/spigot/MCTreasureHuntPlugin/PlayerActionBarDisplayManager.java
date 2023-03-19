package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;

public class PlayerActionBarDisplayManager {
  private static Plugin instance;

  public static void register(Plugin instance) {
    PlayerActionBarDisplayManager.instance = instance;
    Bukkit.getScheduler()
        .scheduleSyncRepeatingTask(
            instance,
            () -> Bukkit.getOnlinePlayers().forEach(PlayerActionBarDisplayManager::tick),
            20,
            20);
  }

  public static void tick(Player player) {
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);

    if (!playerDataResult.isOk()) {
      instance
          .getLogger()
          .warning(
              "Failed to get player data for "
                  + player.getName()
                  + ": "
                  + playerDataResult.getError());
      return;
    }

    PlayerData playerData = playerDataResult.getValue();

    String points = String.valueOf(playerData.getPoints());
    ActionBarManager.sendActionBar(
        player,
        ChatColor.GOLD
            + "★ "
            + ChatColor.BOLD
            + points
            + ChatColor.RESET
            + ""
            + ChatColor.GOLD
            + " ★");
  }
}
