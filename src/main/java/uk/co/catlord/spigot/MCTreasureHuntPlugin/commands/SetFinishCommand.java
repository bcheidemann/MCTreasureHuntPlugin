package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.Checkpoint;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Point;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.CommandUtils;

public class SetFinishCommand extends RegisterableCommand {
  @Override
  public String getName() {
    return "set-finish";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 4) {
      List<String> options = new ArrayList<String>(List.of("<previous-checkpoint>"));
      options.addAll(CheckpointDataStore.getStore().getCheckpointNames());

      return options;
    }

    return CommandUtils.tabCompleteTargetedBlockCoorinates(sender, command, label, args, 0);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // Validate the command
    CommandOptions options = validate(sender, cmd, label, args);

    // Exit early if the command is invalid
    if (options == null) {
      return true;
    }

    // Create the checkpoint
    Checkpoint checkpoint =
        new Checkpoint("FINISH", options.previousCheckpointName, new Point(options.location));
    Result<?, String> result = CheckpointDataStore.getStore().addCheckpoint(checkpoint);

    // Feedback to the player
    if (result.isError()) {
      sender.sendMessage(ChatColor.RED + "Failed to set finish checkpoint: " + result.getError());
    } else {
      sender.sendMessage(ChatColor.GREEN + "Finish checkpoint set successfully.");
    }

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
    if (args.length != 4) {
      sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <x> <y> <z> <previous-checkpoint>");
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

    // Check if the previous checkpoint is valid
    String previousCheckpointName = args[3];

    if (previousCheckpointName == null || previousCheckpointName == "") {
      sender.sendMessage(ChatColor.RED + "The previous checkpoint must be a valid string.");
      return null;
    }

    if (CheckpointDataStore.getStore().getCheckpointByName(previousCheckpointName) == null) {
      sender.sendMessage(
          ChatColor.RED
              + "The previous checkpoint ("
              + previousCheckpointName
              + ") does not exist.");
      return null;
    }

    // Create the command options object
    CommandOptions options = new CommandOptions();

    // Set the location
    options.location = location;

    // Set the previous checkpoint name
    options.previousCheckpointName = previousCheckpointName;

    // Return the command options
    return options;
  }

  class CommandOptions {
    public Location location;
    public String previousCheckpointName;
  }
}
