package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerUtils {
  public static void givePlayerItemStack(HumanEntity player, ItemStack itemStack) {
    PlayerInventory inventory = player.getInventory();

    if (inventory.firstEmpty() == -1) {
      player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
    } else {
      inventory.addItem(itemStack);
    }
  }
}
