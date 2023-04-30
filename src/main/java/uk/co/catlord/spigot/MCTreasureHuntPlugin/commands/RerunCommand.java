package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData.RaceStatus;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;

public class RerunCommand extends RegisterableCommand {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
      return true;
    }

    Player player = (Player) sender;

    if (args.length == 0 || args.length != 1 || !args[0].equals("confirm")) {
      sender.sendMessage(
          ChatColor.RED
              + "IMPORTANT: Running this command will erease your score! If you are not sure you're"
              + " happy with that, please don't run this command. Before you run the command,"
              + " please screenshot the output of the /scores command to evidence the time you got"
              + " on your first run and send it to DippyBlether on Discord. Run '/rerun conform' to"
              + " confirm and restart the hunt.");
      return true;
    }

    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);

    if (playerDataResult.isError()) {
      sender.sendMessage(ChatColor.RED + playerDataResult.getError());
      return true;
    }

    PlayerData playerData = playerDataResult.getValue();

    if (playerData.getRaceStatus() != RaceStatus.FINISHED) {
      sender.sendMessage(ChatColor.RED + "You must have completed the race to re-run it.");
      return true;
    }

    Result<?, String> resetResult = playerData.reset();

    if (resetResult.isError()) {
      sender.sendMessage(ChatColor.RED + resetResult.getError());
      return true;
    }

    sender.sendMessage(ChatColor.GREEN + "Rerunning...");

    player.getInventory().clear();
    player.setHealth(0);

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    return List.of();
  }

  @Override
  protected String getName() {
    return "rerun";
  }
}
