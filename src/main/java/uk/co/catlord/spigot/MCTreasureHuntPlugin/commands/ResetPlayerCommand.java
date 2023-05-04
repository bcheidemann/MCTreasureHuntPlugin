package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;

public class ResetPlayerCommand extends RegisterableCommand {
  @Override
  protected String getName() {
    return "reset-player";
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
      return true;
    }

    if (args.length != 2 || !args[1].equals("confirm")) {
      sender.sendMessage(
          ChatColor.RED
              + "IMPORTANT: Running this command will the players score! If you are sure you"
              + " are happy with that, please run /reset-player <player> confirm.");
      return true;
    }

    String playerName = args[0];
    OfflinePlayer player = null;
    for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
      if (offlinePlayer.getName().equals(playerName)) {
        player = offlinePlayer;
        break;
      }
    }

    if (player == null) {
      sender.sendMessage(ChatColor.RED + "Player not found.");
      return true;
    }

    PlayerData playerData =
        PlayerTrackerDataStore.getStore().getExistingPlayerData(player.getUniqueId());

    if (playerData == null) {
      sender.sendMessage(ChatColor.RED + "Player data not found.");
      return true;
    }

    Result<?, String> resetResult = playerData.reset();

    if (resetResult.isError()) {
      sender.sendMessage(ChatColor.RED + resetResult.getError());
      return true;
    }

    Player onlinePlayer = player.getPlayer();
    if (onlinePlayer != null) {
      onlinePlayer.setGameMode(GameMode.SURVIVAL);
      onlinePlayer.setHealth(0);
      onlinePlayer.getInventory().clear();
    } else {
      for (World world : Bukkit.getWorlds()) {
        File playerDataFile =
            new File(
                Bukkit.getWorlds().get(0).getWorldFolder(),
                "playerdata" + File.separator + player.getUniqueId().toString() + ".dat");

        if (playerDataFile.exists()) {
          try {
            boolean deleted = playerDataFile.delete();
            if (deleted) {
              sender.sendMessage(
                  "Successfully deleted player data for: "
                      + playerName
                      + " in world: "
                      + world.getName());
            } else {
              sender.sendMessage(
                  "Unable to delete player data for: "
                      + playerName
                      + " in world: "
                      + world.getName());
            }
          } catch (Exception e) {
            sender.sendMessage("Error deleting player data: " + e.getMessage());
          }
        }
      }
    }

    sender.sendMessage(ChatColor.GREEN + "Done.");

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length > 1) {
      return List.of();
    }

    List<String> playerNames = new ArrayList<>();

    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      if (player.getName().startsWith(args[0])) {
        playerNames.add(player.getName());
      }
    }

    return playerNames;
  }
}
