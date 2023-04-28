package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.Checkpoint;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Sphere;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.CommandUtils;

public class SetTreasureBeacon extends RegisterableCommand {
  @Override
  public String getName() {
    return "set-treasure-beacon";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 4) {
      return List.of("<name>");
    }

    if (args.length == 5) {
      List<String> options = new ArrayList<String>(List.of("<trail-from>", "START"));
      options.addAll(CheckpointDataStore.getStore().getCheckpointNames());

      return options;
    }

    if (args.length == 6) {
      return List.of("<radius>");
    }

    if (args.length == 7) {
      return List.of("<difficulty>", "EASY", "MEDIUM", "HARD");
    }

    if (args.length > 7) {
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
            null,
            new Sphere(options.location, options.radius),
            Checkpoint.Type.TREASURE_BEACON,
            options.trailFrom,
            options.color);
    Result<?, String> result = CheckpointDataStore.getStore().addCheckpoint(checkpoint);

    // Feedback to the player
    if (result.isError()) {
      options.player.sendMessage(
          ChatColor.RED + "Failed to set treasure beacon checkpoint: " + result.getError());
    } else {
      options.player.sendMessage(ChatColor.GREEN + "Treasure beacon checkpoint set successfully.");
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
    if (args.length != 7) {
      sender.sendMessage(
          ChatColor.RED
              + "USAGE: /"
              + label
              + " <x> <y> <z> <name> <trail-from> <radius> <difficulty>");
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
    String trailFrom = args[4];

    if (trailFrom == null || trailFrom.equals("")) {
      sender.sendMessage(ChatColor.RED + "The previous checkpoint must be a valid string.");
      return null;
    }

    if (!trailFrom.equals("START")
        && CheckpointDataStore.getStore().getCheckpointByName(trailFrom) == null) {
      sender.sendMessage(
          ChatColor.RED + "The previous checkpoint (" + trailFrom + ") does not exist.");
      return null;
    }

    if (trailFrom.equals("FINISH")) {
      sender.sendMessage(
          ChatColor.RED + "Cannot have a treasure trail from the finish checkpoint.");
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

    // Check if the difficulty is valid
    String difficulty = args[6];
    if (difficulty == null
        || difficulty.equals("")
        || (!difficulty.equals("EASY")
            && !difficulty.equals("MEDIUM")
            && !difficulty.equals("HARD"))) {
      sender.sendMessage(ChatColor.RED + "The difficulty must be either EASY, MEDIUM, or HARD.");
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
    options.trailFrom = trailFrom;

    // Set the color
    switch (difficulty) {
      case "EASY":
        options.color = Color.LIME;
        break;
      case "MEDIUM":
        options.color = Color.PURPLE;
        break;
      case "HARD":
        options.color = Color.BLACK;
        break;
    }

    // Return the command options
    return options;
  }

  class CommandOptions {
    public Player player;
    public Location location;
    public String name;
    public String trailFrom;
    public float radius;
    public Color color;
  }
}
