package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChest;
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

  public static String getTreasureChestCheckpoint(Block block) {
    TreasureChest treasureChest =
        TreasureChestDataStore.getStore().getTreasureChest(block.getLocation());

    if (treasureChest == null) {
      return null;
    }

    return treasureChest.checkpointName;
  }

  public static List<TreasureChest> getChestsWithSameCheckpoint(Block block) {
    TreasureChest treasureChest =
        TreasureChestDataStore.getStore().getTreasureChest(block.getLocation());

    if (treasureChest == null || treasureChest.checkpointName == null) {
      return List.of();
    }

    return TreasureChestDataStore.getStore()
        .getTreasureChestsForCheckpoint(treasureChest.checkpointName);
  }
}
