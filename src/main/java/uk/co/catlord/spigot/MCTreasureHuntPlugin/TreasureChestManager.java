package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureChestUtils;

public class TreasureChestManager implements Listener {
    private HashMap<UUID, Inventory> treasureChestInventories = new HashMap<>();

    public static void register(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new TreasureChestManager(), plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        HumanEntity player = event.getPlayer();
        treasureChestInventories.remove(player.getUniqueId());
    }

    public Inventory getTereasureChestInventoryForPlayer(HumanEntity player) {
        player.getUniqueId();
        Inventory inventory = treasureChestInventories.get(player.getUniqueId());
        if (inventory != null) {
            return inventory;
        }
        inventory = Bukkit.createInventory(player, 9, "Treasure Chest");
        treasureChestInventories.put(
                player.getUniqueId(),
                inventory);
        return inventory;
    }

    private boolean isTreasureChestInventory(HumanEntity player, Inventory inventory) {
        Inventory treasureChestInventory = getTereasureChestInventoryForPlayer(player);

        return inventory != null && treasureChestInventory == inventory;
    }

    private boolean isBlockTreasureChest(Block block) {
        return TreasureChestUtils.isBlockTreasureChest(block);
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                isBlockTreasureChest(event.getClickedBlock())
        // TODO: Check if this location is configured as a treasure chest
        // TODO: Check if the player has not already opened a treasure chest
        ) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Inventory inventory = getTereasureChestInventoryForPlayer(player);
            inventory.addItem(new ItemStack(Material.DIAMOND, 1));
            player.openInventory(inventory);
        }
    }

    @EventHandler
    public void onCloseChest(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (isTreasureChestInventory(event.getPlayer(), inventory)) {
            App.instance.getServer().broadcastMessage("Treasure chest inventory closed");
            HumanEntity player = event.getPlayer();
            ItemStack itemStack;
            ListIterator<ItemStack> iterator = inventory.iterator();
            while ((itemStack = iterator.next()) != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
            }
            inventory.clear();
        }
    }
}
