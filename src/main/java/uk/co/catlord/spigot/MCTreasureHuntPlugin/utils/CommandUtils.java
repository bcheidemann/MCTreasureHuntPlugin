package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {
  @FunctionalInterface
  public interface ValidateBlockTargetFunction {
    boolean validate(Location location);
  }

  public static List<String> tabCompleteTargetedBlockCoorinates(
      CommandSender sender, Command command, String label, String[] args, int startAtIndex) {
    return tabCompleteTargetedBlockCoorinates(
        sender, command, label, args, startAtIndex, (location) -> true);
  }

  public static List<String> tabCompleteTargetedBlockCoorinates(
      CommandSender sender,
      Command command,
      String label,
      String[] args,
      int startAtIndex,
      ValidateBlockTargetFunction validateBlockTargetFunction) {
    // Exit early if the sender is not a player
    if (!(sender instanceof Player)) {
      return List.of();
    }

    // Exit early if the args length is not correct
    if (args.length < startAtIndex + 1 || args.length > startAtIndex + 3) {
      return List.of();
    }

    // Cast the sender to a player
    Player player = (Player) sender;

    // Get the location the player is looking at
    Location location = player.getTargetBlock(null, 100).getLocation();

    // Exit early if the player is not looking at a block
    if (location == null) {
      return List.of("<x> <y> <z>");
    }

    // Check the block is not air or null
    if (location.getBlock() == null || location.getBlock().getType().isAir()) {
      return List.of("<x> <y> <z>");
    }

    // Exit early if the block is not a treasure chest
    if (!validateBlockTargetFunction.validate(location)) {
      return List.of("<x> <y> <z>");
    }

    // Tab completion string
    ArrayList<String> tabCompleteString = new ArrayList<>();

    if (args.length == startAtIndex + 1) {
      tabCompleteString.add(Integer.toString(location.getBlockX()));
    }
    if (args.length <= startAtIndex + 2) {
      tabCompleteString.add(Integer.toString(location.getBlockY()));
    }
    if (args.length <= startAtIndex + 3) {
      tabCompleteString.add(Integer.toString(location.getBlockZ()));
    }

    // Return the tab completion string
    return List.of(String.join(" ", tabCompleteString));
  }
}
