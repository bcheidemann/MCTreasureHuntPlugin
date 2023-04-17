package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChestDataStore;

public class TreasureChestUtils {
  public static boolean isBlockTreasureChestLike(Block block) {
    if (block == null) {
      return false;
    }
    Material type = block.getType();
    if (type != Material.CHEST && type != Material.BARREL) {
      return false;
    }
    return true;
  }

  public static boolean isBlockTreasureChest(Block block) {
    return isBlockTreasureChestLike(block)
        && TreasureChestDataStore.getStore().isTreasureChestRegistered(block.getLocation());
  }
}
