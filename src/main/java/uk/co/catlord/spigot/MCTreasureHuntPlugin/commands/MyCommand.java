package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TimeUtils;

public class MyCommand extends RegisterableCommand {
  @Override
  protected String getName() {
    return "my";
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

    switch (options.statType) {
      case TIME:
        sender.sendMessage(
            ""
                + ChatColor.DARK_AQUA
                + ChatColor.BOLD
                + "Time (remaining): "
                + TimeUtils.displaySeconds(playerData.getValue().getTimeRemainingSeconds()));
        break;
      case SCORE:
        sender.sendMessage(
            "" + ChatColor.GOLD + ChatColor.BOLD + "Score: " + playerData.getValue().getPoints());
        break;
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1) {
      return List.of("time", "score");
    }

    return List.of();
  }

  public CommandOptions validate(
      CommandSender sender, Command command, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage(ChatColor.RED + "You must be an op to use this command.");
      return null;
    }

    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
      return null;
    }

    Player player = (Player) sender;

    if (args.length != 1) {
      sender.sendMessage("Usage: /" + label + " <time|score>");
      return null;
    }

    StatType statType;

    switch (args[0]) {
      case "time":
        statType = StatType.TIME;
        break;
      case "score":
        statType = StatType.SCORE;
        break;
      default:
        sender.sendMessage("Usage: /" + label + " <time|score>");
        return null;
    }

    CommandOptions options = new CommandOptions();
    options.player = player;
    options.statType = statType;

    return options;
  }

  public enum StatType {
    TIME,
    SCORE,
  }

  class CommandOptions {
    public Player player;
    public StatType statType;
  }
}
