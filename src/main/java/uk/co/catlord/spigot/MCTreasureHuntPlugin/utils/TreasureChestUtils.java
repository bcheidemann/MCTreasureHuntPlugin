package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class TreasureChestUtils {
    public static boolean isBlockTreasureChest(Block block) {
        if (block == null) {
            return false;
        }
        return (block.getType() == Material.CHEST || block.getType() == Material.BARREL);
    }
}
