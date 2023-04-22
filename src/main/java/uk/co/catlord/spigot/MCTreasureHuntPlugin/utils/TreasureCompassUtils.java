package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TreasureCompassUtils {
  private static String TREASURE_COMPASS_LORE_ID = ">> Treasure Compass <<";

  public static ItemStack createTreasureCompassItemStack() {
    return createTreasureCompassItemStack(1);
  }

  public static ItemStack createTreasureCompassItemStack(int count) {
    // Create a new ItemStack of type COMPASS with the given count
    ItemStack itemStack = new ItemStack(Material.COMPASS, count);

    // Add the VANISHING_CURSE enchantment to the item stack
    itemStack.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);

    // Get the ItemMeta from the item stack
    ItemMeta itemMeta = itemStack.getItemMeta();

    // Set the display name of the item stack
    itemMeta.setDisplayName("Treasure Compass");

    // Set the lore of the item stack
    itemMeta.setLore(
        List.of(
            "",
            "" + ChatColor.GOLD + ChatColor.BOLD + "Treasure Compass",
            ChatColor.GRAY + "The compass points to the next checkpoint but also helps",
            ChatColor.GRAY + "you find treasure beacons!",
            "",
            "" + ChatColor.GREEN + ChatColor.BOLD + "TIP",
            ChatColor.GRAY + "Hold it in your main hand and right click to find treasure",
            "",
            "" + ChatColor.GREEN + ChatColor.BOLD + "TIP",
            ChatColor.GRAY + "If you loose this compass, you can get a new one with the",
            ChatColor.GRAY + "/compass command.",
            "",
            TREASURE_COMPASS_LORE_ID));

    // Set the ItemMeta of the item stack
    itemStack.setItemMeta(itemMeta);

    // Return the item stack
    return itemStack;
  }

  public static boolean isTreasureCompassItemStack(ItemStack itemStack) {
    if (itemStack == null) {
      return false;
    }
    if (itemStack.getType() != Material.COMPASS) {
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
    return lore.contains(TREASURE_COMPASS_LORE_ID);
  }
}
