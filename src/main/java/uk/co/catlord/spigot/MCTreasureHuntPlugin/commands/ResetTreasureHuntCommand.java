package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;

public class ResetTreasureHuntCommand extends RegisterableCommand {
  private static final String PASSWORD = "060697";

  @Override
  public String getName() {
    return "reset-treasure-hunt";
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1) {
      return List.of("<password>");
    }

    return List.of();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // Validate the correct number of arguments were passed
    if (args.length != 1) {
      return false;
    }

    // Check if the sender has op permissions
    if (!sender.isOp()) {
      sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
      return true;
    }

    // Check the password is correct
    if (!PASSWORD.equals(args[0])) {
      sender.sendMessage(ChatColor.RED + "Incorrect password.");
      return true;
    }

    // Reset the treasure hunt
    Result<Boolean, String> resetResult = PlayerTrackerDataStore.getStore().reset();

    // Check if the reset was successful
    if (resetResult.isError()) {
      sender.sendMessage(
          ChatColor.RED + "Failed to reset treasure hunt: " + resetResult.getError());
      sender.sendMessage(ChatColor.RED + "Please contact an administrator.");
      return true;
    }

    // Confirm the reset
    sender.sendMessage(ChatColor.GREEN + "Treasure hunt reset successfully.");

    return true;
  }
}
