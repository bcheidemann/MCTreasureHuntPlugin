package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

// An example event listener to show how to use the ActionBarManager
public class EventListener implements Listener {
    public static void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new EventListener(), plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        ActionBarManager.sendActionBar(event.getPlayer(), String.format("X: %d, Y: %d, Z: %d",
                event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ()));
    }
}
