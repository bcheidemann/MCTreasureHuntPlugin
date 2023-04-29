package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TimeShardDepositChestUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureTokenUtils;

public class TimeShardDepositChestInventoryManager implements Listener {
  private HashMap<UUID, Inventory> timeShardDepositChestInventories = new HashMap<>();

  public static void register(JavaPlugin plugin) {
    plugin
        .getServer()
        .getPluginManager()
        .registerEvents(new TimeShardDepositChestInventoryManager(), plugin);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    HumanEntity player = event.getPlayer();
    timeShardDepositChestInventories.remove(player.getUniqueId());
  }

  public Inventory getEmptyTimeShardDepositChestInventoryForPlayer(HumanEntity player) {
    return getTimeShardDepositChestInventoryForPlayer(player, true);
  }

  public Inventory getTimeShardDepositChestInventoryForPlayer(HumanEntity player) {
    return getTimeShardDepositChestInventoryForPlayer(player, false);
  }

  public Inventory getTimeShardDepositChestInventoryForPlayer(
      HumanEntity player, boolean ensureEmpty) {
    player.getUniqueId();
    Inventory inventory = timeShardDepositChestInventories.get(player.getUniqueId());
    if (inventory != null) {
      if (ensureEmpty) {
        inventory.clear();
      }
      return inventory;
    }
    inventory = Bukkit.createInventory(player, 27, "Time Shard Deposit Chest");
    timeShardDepositChestInventories.put(player.getUniqueId(), inventory);
    return inventory;
  }

  private boolean isTimeShardDepositChestInventory(HumanEntity player, Inventory inventory) {
    Inventory timeShardDepositChestInventory = getTimeShardDepositChestInventoryForPlayer(player);

    return inventory != null && timeShardDepositChestInventory == inventory;
  }

  @EventHandler
  public void onChestOpen(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK
        && TimeShardDepositChestUtils.isTimeShardDepositChest(event.getClickedBlock())) {
      event.setCancelled(true);
      Player player = event.getPlayer();
      Inventory inventory = getEmptyTimeShardDepositChestInventoryForPlayer(player);
      player.openInventory(inventory);
    }
  }

  @EventHandler
  public void onCloseChest(InventoryCloseEvent event) {
    Inventory inventory = event.getInventory();
    if (!isTimeShardDepositChestInventory(event.getPlayer(), inventory)) {
      return;
    }
    HumanEntity player = event.getPlayer();
    int minutes = 0;
    for (int i = 0; i < 27; i++) {
      ItemStack itemStack = inventory.getItem(i);
      if (itemStack != null && TreasureTokenUtils.isTreasureTokenItemStack(itemStack)) {
        minutes += itemStack.getAmount();
      }
    }
    inventory.clear();
    PlayerUtils.givePlayerTimeSeconds(player, minutes * 60);
    inventory.clear();
  }
}
