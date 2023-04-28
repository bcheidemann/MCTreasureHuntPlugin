package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureTokenUtils;

public class GiveTreasureTokenCommand extends RegisterableCommand {
  @Override
  public String getName() {
    return "give-treasure-token";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    // Exit early if the sender is not a player
    if (!(sender instanceof Player)) {
      return List.of();
    }

    if (args.length == 1) {
      return List.of("1", "2", "4", "8");
    }

    return List.of();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // Validate the command
    CommandOptions options = validate(sender, cmd, label, args);

    // Exit early if the command is invalid
    if (options == null) {
      return true;
    }

    // Give the player a Time Shard
    ItemStack treasureTokenItemStack =
        TreasureTokenUtils.createTreasureTokenItemStack(options.count);
    PlayerUtils.givePlayerItemStack(options.player, treasureTokenItemStack);

    // Return true
    return true;
  }

  private CommandOptions validate(CommandSender sender, Command cmd, String label, String[] args) {
    // Check if the sender has op permissions
    if (!sender.isOp()) {
      sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
      return null;
    }

    // Check if the sender is a player
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
      return null;
    }

    // Cast the sender to a player
    Player player = (Player) sender;

    // Check if the command has the correct number of arguments
    if (args.length > 1) {
      sender.sendMessage(ChatColor.RED + "USAGE: /" + label + " <x> <y> <z>");
      return null;
    }

    // Get the count
    int count;

    // If no arguments are provided, default to 1
    if (args.length == 0) {
      count = 1;
    }
    // Otherwise, parse the count argument
    else {
      try {
        count = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "The count must be a valid integer.");
        return null;
      }
    }

    // Create the command options object
    CommandOptions options = new CommandOptions();

    // Set the player
    options.player = player;

    // Set the count
    options.count = count;

    // Return the command options
    return options;
  }

  class CommandOptions {
    public Player player;
    public int count;
  }
}
