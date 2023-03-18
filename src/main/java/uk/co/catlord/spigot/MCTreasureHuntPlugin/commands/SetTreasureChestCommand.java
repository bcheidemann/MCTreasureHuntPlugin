package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChest;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChestDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureChestUtils;

public class SetTreasureChestCommand extends RegisterableCommand {
  @Override
  public String getName() {
    return "set-treasure-chest";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    // Exit early if the sender is not a player
    if (!(sender instanceof Player)) {
      return List.of();
    }

    // Cast the sender to a player
    Player player = (Player) sender;

    // Get the location the player is looking at
    Location location = player.getTargetBlock(null, 100).getLocation();

    // Exit early if the player is not looking at a block
    if (location == null) {
      return List.of();
    }

    // Exit early if the block is not a treasure chest
    if (!TreasureChestUtils.isBlockTreasureChestLike(location.getBlock())) {
      return List.of();
    }

    // Tab completion string
    ArrayList<String> tabCompleteString = new ArrayList<>();

    if (args.length == 1) {
      tabCompleteString.add(Integer.toString(location.getBlockX()));
    }
    if (args.length <= 2) {
      tabCompleteString.add(Integer.toString(location.getBlockY()));
    }
    if (args.length <= 3) {
      tabCompleteString.add(Integer.toString(location.getBlockZ()));
    }

    // Return the tab completion string
    return List.of(String.join(" ", tabCompleteString));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // Validate the command
    CommandOptions options = validate(sender, cmd, label, args);

    // Exit early if the command is invalid
    if (options == null) {
      return true;
    }

    // Create the treasure chest
    TreasureChest treasureChest = new TreasureChest(options.location);
    Result<?, String> result = TreasureChestDataStore.getStore().addTreasureChest(treasureChest);

    // Feedback to the player
    if (result.isError()) {
      options.player.sendMessage(
          ChatColor.RED + "Failed to set treasure chest: " + result.getError());
    } else {
      options.player.sendMessage(ChatColor.GREEN + "Treasure chest set successfully.");

      // Flash the treasure chest block to indicate it has been set
      Material material = options.location.getBlock().getType();
      BlockData blockData = options.location.getBlock().getBlockData();
      treasureChest.location.getWorld().setType(treasureChest.location, Material.EMERALD_BLOCK);
      Bukkit.getScheduler()
          .runTaskLater(
              App.instance,
              () -> {
                treasureChest.location.getWorld().setType(treasureChest.location, material);
                treasureChest.location.getWorld().setBlockData(treasureChest.location, blockData);
              },
              10);
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

    // Ensure the location is a valid block location
    if (!TreasureChestUtils.isBlockTreasureChestLike(location.getBlock())) {
      sender.sendMessage(
          ChatColor.RED
              + "The location must be a chest or barrel. ("
              + location.getBlock().getType().toString()
              + ")");
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
