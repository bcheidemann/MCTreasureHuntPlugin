package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TreasureTokenUtils {
  private static String TREASURE_TOKEN_LORE_ID = ">> Treasure Token <<";

  public static ItemStack createTreasureTokenItemStack(int count) {
    ItemStack itemStack = new ItemStack(Material.AMETHYST_SHARD, count);
    itemStack.addUnsafeEnchantment(Enchantment.MENDING, 1);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName("Treasure Token");
    itemMeta.setLore(
        List.of(
            "Each token worth 1 minute when cached in at a checkpoint", TREASURE_TOKEN_LORE_ID));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }
}
