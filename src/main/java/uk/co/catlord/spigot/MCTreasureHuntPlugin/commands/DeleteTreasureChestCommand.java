package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChestDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.CommandUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureChestUtils;

public class DeleteTreasureChestCommand extends RegisterableCommand {
  @Override
  public String getName() {
    return "delete-treasure-chest";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    return CommandUtils.tabCompleteTargetedBlockCoorinates(
        sender,
        command,
        label,
        args,
        0,
        (location) -> TreasureChestUtils.isBlockTreasureChestLike(location.getBlock()));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // Validate the command
    CommandOptions options = validate(sender, cmd, label, args);

    // Exit early if the command is invalid
    if (options == null) {
      return true;
    }

    // Remove the treasure chest
    Result<?, String> result =
        TreasureChestDataStore.getStore().removeTreasureChest(options.location);

    // Feedback to the player
    if (result.isError()) {
      options.player.sendMessage(
          ChatColor.RED + "Failed to delete treasure chest: " + result.getError());
      return true;
    }

    options.location.getBlock().setType(Material.AIR);
    options.player.sendMessage(ChatColor.GREEN + "Treasure chest set successfully.");

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
    if (args.length != 3) {
      sender.sendMessage(ChatColor.RED + "USAGE: /" + label + " <x> <y> <z>");
      return null;
    }

    // Check if the arguments are valid numbers
    int x, y, z;

    // Parse the x coordinate
    try {
      x = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatColor.RED + "The x coordinate must be a valid integer.");
      return null;
    }

    // Parse the y coordinate
    try {
      y = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatColor.RED + "The y coordinate must be a valid integer.");
      return null;
    }

    // Parse the z coordinate
    try {
      z = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatColor.RED + "The z coordinate must be a valid integer.");
      return null;
    }

    // Create the location object
    Location location = new Location(player.getWorld(), x, y, z);

    // Ensure there is a treasure chest registered at the location
    if (!TreasureChestDataStore.getStore().isTreasureChestRegistered(location)) {
      sender.sendMessage(
          ChatColor.YELLOW
              + "The location is not a registered treasure chest. Use /set-treasure-chest to"
              + " register a treasure chest.");
      return null;
    }

    // Create the command options object
    CommandOptions options = new CommandOptions();

    // Set the player
    options.player = player;

    // Set the location
    options.location = location;

    // Return the command options
    return options;
  }

  class CommandOptions {
    public Player player;
    public Location location;
  }
}
