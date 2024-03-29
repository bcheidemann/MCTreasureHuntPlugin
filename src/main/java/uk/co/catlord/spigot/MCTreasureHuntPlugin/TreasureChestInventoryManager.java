package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
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
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.Checkpoint;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChest;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureChestUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureTokenUtils;

public class TreasureChestInventoryManager implements Listener {
  private HashMap<UUID, Inventory> treasureChestInventories = new HashMap<>();

  public static void register(JavaPlugin plugin) {
    plugin
        .getServer()
        .getPluginManager()
        .registerEvents(new TreasureChestInventoryManager(), plugin);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    HumanEntity player = event.getPlayer();
    treasureChestInventories.remove(player.getUniqueId());
  }

  public Inventory getEmptyTreasureChestInventoryForPlayer(HumanEntity player) {
    return getTereasureChestInventoryForPlayer(player, true);
  }

  public Inventory getTereasureChestInventoryForPlayer(HumanEntity player) {
    return getTereasureChestInventoryForPlayer(player, false);
  }

  public Inventory getTereasureChestInventoryForPlayer(HumanEntity player, boolean ensureEmpty) {
    player.getUniqueId();
    Inventory inventory = treasureChestInventories.get(player.getUniqueId());
    if (inventory != null) {
      if (ensureEmpty) {
        inventory.clear();
      }
      return inventory;
    }
    inventory = Bukkit.createInventory(player, 27, "Treasure Chest");
    treasureChestInventories.put(player.getUniqueId(), inventory);
    return inventory;
  }

  private boolean isTreasureChestInventory(HumanEntity player, Inventory inventory) {
    Inventory treasureChestInventory = getTereasureChestInventoryForPlayer(player);

    return inventory != null && treasureChestInventory == inventory;
  }

  private boolean isBlockTreasureChest(Block block) {
    return TreasureChestUtils.isBlockTreasureChest(block);
  }

  private Inventory getTreasureChestBlockInventory(Block block) {
    BlockState blockState = block.getState();

    if (blockState instanceof Chest) {
      Chest chest = (Chest) blockState;
      return chest.getInventory();
    }

    if (blockState instanceof DoubleChest) {
      DoubleChest doubleChest = (DoubleChest) blockState;
      return doubleChest.getInventory();
    }

    if (blockState instanceof Barrel) {
      Barrel barrel = (Barrel) blockState;
      return barrel.getInventory();
    }

    return null;
  }

  @EventHandler
  public void onChestOpen(PlayerInteractEvent event) {
    if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
      return;
    }
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK
        && isBlockTreasureChest(event.getClickedBlock())) {
      event.setCancelled(true);
      boolean hasPlayerOpenedTreasureChest =
          PlayerUtils.hasPlayerOpenedTreasureChest(event.getPlayer(), event.getClickedBlock());
      if (hasPlayerOpenedTreasureChest) {
        // Show title to player
        event
            .getPlayer()
            .sendTitle(
                ChatColor.DARK_RED + "Chest Looted",
                ChatColor.GRAY + "You have already looted this chest",
                0,
                40,
                10);
        return;
      }
      Player player = event.getPlayer();
      Inventory inventory = getEmptyTreasureChestInventoryForPlayer(player);
      Inventory templateInventory = getTreasureChestBlockInventory(event.getClickedBlock());
      int points = 0;
      for (int i = 0; i < 27; i++) {
        ItemStack itemStack = templateInventory.getItem(i);
        if (itemStack != null) {
          inventory.setItem(i, itemStack.clone());
          if (TreasureTokenUtils.isTreasureTokenItemStack(itemStack)) {
            points += itemStack.getAmount();
          }
        }
      }
      player.openInventory(inventory);
      PlayerUtils.givePlayerPoints(player, points);
      PlayerUtils.setPlayerOpenedTreasureChest(player, event.getClickedBlock());

      // Count chests in area
      String checkpointName =
          TreasureChestUtils.getTreasureChestCheckpoint(event.getClickedBlock());
      List<TreasureChest> chestsInArea =
          TreasureChestUtils.getChestsWithSameCheckpoint(event.getClickedBlock());
      Result<PlayerData, String> playerDataResult =
          PlayerTrackerDataStore.getStore().getPlayerData(player);
      if (playerDataResult.isError()) {
        player.sendMessage("Error: " + playerDataResult.getError());
        return;
      }
      PlayerData playerData = playerDataResult.getValue();
      int chestsLooted = 0;
      for (TreasureChest treasureChest : chestsInArea) {
        if (playerData.hasOpenedTreasureChest(treasureChest.uuid)) {
          chestsLooted++;
        }
      }

      // Show title to player
      Checkpoint checkpoint = CheckpointDataStore.getStore().getCheckpointByName(checkpointName);
      if (checkpoint == null) {
        checkpointName = "in the wilderness";
      } else if (checkpoint.type == Checkpoint.Type.CHECKPOINT) {
        if (checkpointName.equals("START")) {
          checkpointName = "the start";
        } else if (checkpointName.equals("FINISH")) {
          checkpointName = "the finish";
        }
        checkpointName = "in the wilderness near " + checkpointName;
      } else if (checkpoint.type == Checkpoint.Type.TREASURE_BEACON) {
        checkpointName = "at " + checkpointName;
      }

      event
          .getPlayer()
          .sendMessage(
              "" + ChatColor.GRAY + ChatColor.BOLD + "Chest Looted! ",
              ChatColor.GRAY
                  + "("
                  + chestsLooted
                  + " / "
                  + chestsInArea.size()
                  + " "
                  + checkpointName
                  + ")");
    }
  }

  @EventHandler
  public void onCloseChest(InventoryCloseEvent event) {
    Inventory inventory = event.getInventory();
    if (!isTreasureChestInventory(event.getPlayer(), inventory)) {
      return;
    }
    HumanEntity player = event.getPlayer();
    ItemStack itemStack;
    for (int i = 0; i < 27; i++) {
      itemStack = inventory.getItem(i);
      if (itemStack != null) {
        PlayerUtils.givePlayerItemStack(player, itemStack);
      }
    }
    inventory.clear();
  }
}
