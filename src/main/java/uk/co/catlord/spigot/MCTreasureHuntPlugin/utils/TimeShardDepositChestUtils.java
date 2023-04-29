package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.time_shard_deposit_chests.TimeShardDepositChestDataStore;

public class TimeShardDepositChestUtils {
  public static boolean isTimeShardDepositChestLike(Block block) {
    if (block == null) {
      return false;
    }
    Material type = block.getType();
    if (type != Material.CHEST && type != Material.BARREL) {
      return false;
    }
    return true;
  }

  public static boolean isTimeShardDepositChest(Block block) {
    return isTimeShardDepositChestLike(block)
        && TimeShardDepositChestDataStore.getStore()
            .isTimeShardDepositChestRegisteredAt(block.getLocation());
  }
}
