package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.TreasureCompassUtils;

public class CompassCommand extends RegisterableCommand {
  @Override
  protected String getName() {
    return "compass";
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("This command can only be run by a player.");
      return true;
    }

    Player player = (Player) sender;

    PlayerUtils.givePlayerItemStack(player, TreasureCompassUtils.createTreasureCompassItemStack());

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    return List.of();
  }
}
