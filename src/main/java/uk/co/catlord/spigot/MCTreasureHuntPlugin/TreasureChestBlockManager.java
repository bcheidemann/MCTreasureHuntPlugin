package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureChestUtils;

public class TreasureChestBlockManager implements Listener {
  public static void register(JavaPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(new TreasureChestBlockManager(), plugin);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    if (!TreasureChestUtils.isBlockTreasureChest(block)) {
      return;
    }

    event
        .getPlayer()
        .sendMessage(
            ChatColor.RED
                + "You cannot break treasure chests! Use the /delete-treasure-chest command"
                + " instead.");
    event.setCancelled(true);
  }
}
