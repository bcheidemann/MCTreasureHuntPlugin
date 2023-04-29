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
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.CommandUtils;

public class SetCheckpointRespawnPointCommand extends RegisterableCommand {
  @Override
  public String getName() {
    return "set-checkpoint-respawn-point";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 4) {
      List<String> options = new ArrayList<String>(List.of("<checkpoint>"));
      options.addAll(CheckpointDataStore.getStore().getCheckpointNames());

      return options;
    }

    if (args.length > 4) {
      return List.of();
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

    Result<?, String> setRespawnPointResult = options.checkpoint.setRespawnPoint(options.location);

    // Check if the respawn point was set successfully
    if (setRespawnPointResult.isError()) {
      sender.sendMessage(
          ChatColor.RED
              + "Failed to set respawn point for checkpoint: "
              + setRespawnPointResult.getError());
      return true;
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
      sender.sendMessage(ChatColor.RED + "USAGE: /" + label + " <x> <y> <z> <checkpoint>");
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

    // Check if the checkpoint is valid
    String checkpointName = args[3];

    if (checkpointName == null || checkpointName.equals("")) {
      sender.sendMessage(ChatColor.RED + "The previous checkpoint must be a valid string.");
      return null;
    }

    Checkpoint checkpoint = CheckpointDataStore.getStore().getCheckpointByName(checkpointName);
    if (checkpoint == null) {
      sender.sendMessage(ChatColor.RED + "The checkpoint (" + checkpointName + ") does not exist.");
      return null;
    }

    // Create the location object
    Location location = new Location(player.getWorld(), x, y, z);

    // Create the command options object
    CommandOptions options = new CommandOptions();

    // Set the player
    options.player = player;

    // Set the location
    options.location = location;

    // Set the previous checkpoint name
    options.checkpoint = checkpoint;

    // Return the command options
    return options;
  }

  class CommandOptions {
    public Player player;
    public Location location;
    public Checkpoint checkpoint;
  }
}
