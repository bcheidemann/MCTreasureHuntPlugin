package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.bukkit.ChatColor;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;

public class BroadcastUtils {
  public static void broadcastRaceEvent(String message) {
    App.instance.getServer().broadcastMessage("" + ChatColor.GRAY + ChatColor.BOLD + message);
  }
}
