package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData.RaceStatus;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;

public class ScoresCommand extends RegisterableCommand {
  @Override
  protected String getName() {
    return "scores";
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    StringBuilder started =
        new StringBuilder()
            .append(ChatColor.AQUA)
            .append(ChatColor.BOLD)
            .append(ChatColor.UNDERLINE)
            .append("Started\n")
            .append(ChatColor.RESET)
            .append(ChatColor.GRAY);
    StringBuilder finished =
        new StringBuilder()
            .append(ChatColor.GREEN)
            .append(ChatColor.BOLD)
            .append(ChatColor.UNDERLINE)
            .append("Finished\n")
            .append(ChatColor.RESET)
            .append(ChatColor.GRAY);

    for (PlayerData playerData : PlayerTrackerDataStore.getStore().players.values()) {
      RaceStatus raceStatus = playerData.getRaceStatus();

      if (raceStatus == RaceStatus.NOT_STARTED) {
        continue;
      }

      UUID playerUuid = playerData.uuid;
      String playerName = getPlayerName(playerUuid);
      int score = playerData.getPoints();

      if (raceStatus == RaceStatus.STARTED) {
        started
            .append(playerName)
            .append(": ")
            .append(ChatColor.BOLD)
            .append(score)
            .append(ChatColor.RESET)
            .append(ChatColor.GRAY)
            .append("\n");
      } else if (raceStatus == RaceStatus.FINISHED) {
        finished
            .append(playerName)
            .append(": ")
            .append(ChatColor.BOLD)
            .append(score)
            .append(ChatColor.RESET)
            .append(ChatColor.GRAY)
            .append("\n");
      }
    }

    sender.sendMessage(started.toString() + "\n" + finished.toString());

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    return List.of();
  }

  private String getPlayerName(UUID playerUuid) {
    Player onlinePlayer = Bukkit.getPlayer(playerUuid);
    if (onlinePlayer != null) {
      return onlinePlayer.getName();
    }

    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
    if (offlinePlayer != null) {
      return offlinePlayer.getName();
    }

    return "Unknown player";
  }
}
