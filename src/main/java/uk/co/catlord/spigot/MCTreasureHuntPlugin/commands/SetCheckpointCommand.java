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
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Sphere;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.CommandUtils;

public class SetCheckpointCommand extends RegisterableCommand {
  @Override
  public String getName() {
    return "set-checkpoint";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 4) {
      return List.of("<name>");
    }

    if (args.length == 5) {
      List<String> options = new ArrayList<String>(List.of("<previous-checkpoint>", "START"));
      options.addAll(CheckpointDataStore.getStore().getCheckpointNames());

      return options;
    }

    if (args.length == 6) {
      return List.of("<radius>");
    }

    if (args.length > 6) {
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

    // Create the checkpoint
    Checkpoint checkpoint =
        new Checkpoint(
            options.name,
            options.previousCheckpointName,
            new Sphere(options.location, options.radius),
            Checkpoint.Type.CHECKPOINT,
            null,
            null);
    Result<?, String> result = CheckpointDataStore.getStore().addCheckpoint(checkpoint);

    // Feedback to the player
    if (result.isError()) {
      options.player.sendMessage(ChatColor.RED + "Failed to checkpoint: " + result.getError());
    } else {
      options.player.sendMessage(ChatColor.GREEN + "Checkpoint set successfully.");
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
    if (args.length != 6) {
      sender.sendMessage(
          ChatColor.RED
              + "USAGE: /"
              + label
              + " <x> <y> <z> <name> <previous-checkpoint> <radius>");
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

    // Check if the name is valid
    String name = args[3];

    if (name == null || name.equals("")) {
      sender.sendMessage(ChatColor.RED + "The name must be a valid string.");
      return null;
    }

    if (CheckpointDataStore.getStore().getCheckpointByName(name) != null) {
      sender.sendMessage(ChatColor.RED + "A checkpoint with that name already exists.");
      return null;
    }

    // Check if the previous checkpoint is valid
    String previousCheckpointName = args[4];

    if (previousCheckpointName == null || previousCheckpointName.equals("")) {
      sender.sendMessage(ChatColor.RED + "The previous checkpoint must be a valid string.");
      return null;
    }

    if (!previousCheckpointName.equals("START")
        && CheckpointDataStore.getStore().getCheckpointByName(previousCheckpointName) == null) {
      sender.sendMessage(
          ChatColor.RED
              + "The previous checkpoint ("
              + previousCheckpointName
              + ") does not exist.");
      return null;
    }

    // Check if the radius is valid
    float radius;

    try {
      radius = Float.parseFloat(args[5]);
    } catch (NumberFormatException e) {
      sender.sendMessage(ChatColor.RED + "The radius must be a valid number.");
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

    // Set the name
    options.name = name;

    // Set the radius
    options.radius = radius;

    // Set the previous checkpoint name
    options.previousCheckpointName = previousCheckpointName;

    // Return the command options
    return options;
  }

  class CommandOptions {
    public Player player;
    public Location location;
    public String name;
    public String previousCheckpointName;
    public float radius;
  }
}
