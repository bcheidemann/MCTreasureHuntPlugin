package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;

public class SetPlayerTimeCommand extends RegisterableCommand {
  @Override
  protected String getName() {
    return "set-player-time";
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    CommandOptions options = validate(sender, command, label, args);

    if (options == null) {
      return true;
    }

    Result<PlayerData, String> playerData =
        PlayerTrackerDataStore.getStore().getPlayerData(options.player);

    if (playerData.isError()) {
      sender.sendMessage(ChatColor.RED + "Failed to get player data: " + playerData.getError());
      return true;
    }

    Result<Boolean, String> setTimeResult =
        playerData.getValue().setTimeRemainingSeconds(options.timeSeconds);

    if (setTimeResult.isError()) {
      sender.sendMessage(ChatColor.RED + "Failed to set time: " + setTimeResult.getError());
      return true;
    }

    sender.sendMessage(ChatColor.GREEN + "Time set successfully.");

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1) {
      List<String> playerNames = new ArrayList<>();

      for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getName().startsWith(args[0])) {
          playerNames.add(player.getName());
        }
      }

      if (playerNames.isEmpty()) {
        return List.of("<player>");
      }

      return playerNames;
    }

    if (args.length == 2) {
      return List.of("<hours>");
    }

    if (args.length == 3) {
      return List.of("<minutes>");
    }

    if (args.length == 4) {
      return List.of("<seconds>");
    }

    return List.of();
  }

  private CommandOptions validate(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
      return null;
    }

    if (args.length < 2 || args.length > 4) {
      sender.sendMessage("Usage: /set-player-time <player> <hours> <minutes> <seconds>");
      return null;
    }

    Player player = Bukkit.getPlayer(args[0]);

    if (player == null) {
      sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found.");
      return null;
    }

    int timeSeconds = 0;

    try {
      if (args.length >= 2) {
        timeSeconds += Integer.parseInt(args[1]) * 3600;
      }

      if (args.length >= 3) {
        timeSeconds += Integer.parseInt(args[2]) * 60;
      }

      if (args.length >= 4) {
        timeSeconds += Integer.parseInt(args[3]);
      }
    } catch (Exception e) {
      sender.sendMessage(ChatColor.RED + "Invalid time format.");
      sender.sendMessage(e.getMessage());
      return null;
    }

    CommandOptions options = new CommandOptions();
    options.player = player;
    options.timeSeconds = timeSeconds;

    return options;
  }

  class CommandOptions {
    public Player player;
    public int timeSeconds;
  }
}
