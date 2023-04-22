package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

public class TimeUtils {
  public static String displaySeconds(int totalSeconds) {
    int hours = Math.floorDiv(totalSeconds, 3600);
    int minutes = Math.floorDiv(totalSeconds - (hours * 3600), 60);
    int seconds = totalSeconds - (hours * 3600) - (minutes * 60);

    if (hours > 0) {
      return String.format("%dhr %02dm", hours, minutes);
    }

    if (minutes > 9) {
      return String.format("%dmin", minutes);
    }

    if (minutes > 0) {
      return String.format("%dmin %02ds", minutes, seconds);
    }

    return String.format("%ds", seconds);
  }
}
