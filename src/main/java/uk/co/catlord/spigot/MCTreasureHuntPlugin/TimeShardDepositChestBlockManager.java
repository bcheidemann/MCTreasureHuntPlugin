package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TimeShardDepositChestUtils;

public class TimeShardDepositChestBlockManager implements Listener {
  public static void register(JavaPlugin plugin) {
    plugin
        .getServer()
        .getPluginManager()
        .registerEvents(new TimeShardDepositChestBlockManager(), plugin);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    if (!TimeShardDepositChestUtils.isTimeShardDepositChest(block)) {
      return;
    }

    event
        .getPlayer()
        .sendMessage(
            ChatColor.RED
                + "You cannot break a time shard deposit chest! Please stop the server, delete it"
                + " from timeShardDepositChests.json, then restart the server.");
    event.setCancelled(true);
  }
}
