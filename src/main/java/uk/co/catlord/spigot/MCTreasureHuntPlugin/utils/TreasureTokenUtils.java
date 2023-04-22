package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.util.List;
import net.md_5.bungee.api.ChatColor;
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
            "",
            "" + ChatColor.GOLD + ChatColor.BOLD + "Treasure Token",
            ChatColor.GRAY + "You receive one point per token you collect, immediately",
            ChatColor.GRAY + "upon collecting it, and you can cache in each token at a",
            ChatColor.GRAY + "checkpoint to add an extra minute back onto your time.",
            "",
            TREASURE_TOKEN_LORE_ID));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static boolean isTreasureTokenItemStack(ItemStack itemStack) {
    if (itemStack == null) {
      return false;
    }
    if (itemStack.getType() != Material.AMETHYST_SHARD) {
      return false;
    }
    if (!itemStack.hasItemMeta()) {
      return false;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (!itemMeta.hasLore()) {
      return false;
    }
    List<String> lore = itemMeta.getLore();
    if (lore == null) {
      return false;
    }
    return lore.contains(TREASURE_TOKEN_LORE_ID);
  }
}
