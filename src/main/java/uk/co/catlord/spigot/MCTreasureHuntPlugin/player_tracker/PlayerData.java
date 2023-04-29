package uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.Checkpoint;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.BroadcastUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;

public class PlayerData {
  public enum RaceStatus {
    NOT_STARTED,
    STARTED,
    FINISHED,
  }

  private PlayerTrackerDataStore store;
  public UUID uuid;
  private int points;
  private Set<UUID> openedTreasureChests = new HashSet<>();
  private int timeRemainingSeconds = 60 * 60 * 3; // 3 hours
  private Set<String> visitedCheckpoints = new HashSet<>();
  private String currentCheckpointName = "START";
  private RaceStatus raceStatus = RaceStatus.NOT_STARTED;
  public Location respawnPoint = null;

  private PlayerData() {}

  public PlayerData(HumanEntity player) {
    this.uuid = player.getUniqueId();
    this.points = 0;
  }

  public void bindToPlayerTrackerDataStore(PlayerTrackerDataStore store) {
    this.store = store;
  }

  private Result<Boolean, String> save() {
    if (store != null) {
      return store.savePlayerTracker();
    }
    return Result.ok(true);
  }

  public Result<Boolean, String> visitCheckpoint(String checkpointName) {
    visitedCheckpoints.add(checkpointName);
    currentCheckpointName = checkpointName;
    return save();
  }

  public boolean hasVisitedCheckpoint(String checkpointName) {
    return visitedCheckpoints.contains(checkpointName);
  }

  public Result<Boolean, String> visitTreasureBeaconCheckpoint(String checkpointName) {
    visitedCheckpoints.add(checkpointName);
    return save();
  }

  public String getCurrentCheckpointName() {
    return currentCheckpointName;
  }

  public Result<Boolean, String> addPoints(int points) {
    this.points += points;
    return save();
  }

  public int getPoints() {
    return points;
  }

  public Result<Boolean, String> addTimeSeconds(int seconds) {
    timeRemainingSeconds += seconds;
    return save();
  }

  public Result<Boolean, String> setRaceStatus(RaceStatus raceStatus) {
    this.raceStatus = raceStatus;
    return save();
  }

  public RaceStatus getRaceStatus() {
    return raceStatus;
  }

  public Result<Boolean, String> setRespawnPoint(Location respawnPoint) {
    this.respawnPoint = respawnPoint;
    return save();
  }

  public Result<Boolean, String> addOpenedTreasureChest(UUID treasureChestUuid) {
    openedTreasureChests.add(treasureChestUuid);
    return save();
  }

  public boolean hasOpenedTreasureChest(UUID treasureChestUuid) {
    return openedTreasureChests.contains(treasureChestUuid);
  }

  public boolean hasTimeRemaining() {
    return timeRemainingSeconds > 0;
  }

  public Result<Boolean, String> setTimeRemainingSeconds(int timeRemainingSeconds) {
    this.timeRemainingSeconds = timeRemainingSeconds;
    return save();
  }

  public int getTimeRemainingSeconds() {
    return timeRemainingSeconds;
  }

  public void tickSecondWithoutSave() {
    if (raceStatus != RaceStatus.STARTED) {
      return;
    }

    // Skip players who have run out of time
    if (!hasTimeRemaining()) {
      return;
    }

    Player player = App.instance.getServer().getPlayer(uuid);

    // Skip offline players
    if (player == null || !player.isOnline()) {
      return;
    }

    // Skip players who are not in survival mode
    if (player.getGameMode() != GameMode.SURVIVAL) {
      return;
    }

    // Skip dead players
    if (player.isDead()) {
      return;
    }

    // Stop time if at checkpoints
    for (Checkpoint checkpoint : CheckpointDataStore.getStore().checkpoints) {
      if (checkpoint.type != Checkpoint.Type.CHECKPOINT) {
        continue;
      }
      if (checkpoint.shape.contains(player.getLocation())) {
        return;
      }
    }

    timeRemainingSeconds -= 1;

    // TODO: Move this somewhere else. This isn't really the responsibility of this class
    // If the player has run out of time, make them a spectator
    // and notify the other players
    if (!hasTimeRemaining()) {
      player.setGameMode(GameMode.SPECTATOR);

      // Strike a lightning effect at the player's location
      player.getWorld().strikeLightningEffect(player.getLocation());

      // Play the lightning sound to all players
      PlayerUtils.playSoundToAllPlayers(Sound.ENTITY_LIGHTNING_BOLT_IMPACT);

      // Send a message to all players
      PlayerUtils.sendTitleToAllPlayers(ChatColor.RED + player.getName(), "ran out of time!");

      // Send chat message to all players
      Result<PlayerData, String> playerDataResult =
          PlayerTrackerDataStore.getStore().getPlayerData(player);

      if (playerDataResult.isError()) {
        player.sendMessage(
            ChatColor.RED
                + "Failed to get player data on elimination (report this to an admin): "
                + playerDataResult.getError());
        return;
      }

      PlayerData playerData = playerDataResult.getValue();
      int finalPoints = playerData.getPoints();
      BroadcastUtils.broadcastRaceEvent(
          ChatColor.RED
              + player.getName()
              + " ran out of time with a score of "
              + finalPoints
              + " points!");
      playerData.setRaceStatus(RaceStatus.FINISHED);
    }
  }

  public static Result<PlayerData, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    PlayerData playerData = new PlayerData();
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse player data");

    boolean hasUuid = value.has("uuid");
    boolean hasPoints = value.has("points");
    boolean hasOpenedTreasureChests = value.has("openedTreasureChests");
    boolean hasTimeRemainingSeconds = value.has("timeRemainingSeconds");
    boolean hasVisitedCheckpoints = value.has("visitedCheckpoints");
    boolean hasCurrentCheckpointName = value.has("currentCheckpointName");
    boolean hasRaceStatus = value.has("raceStatus");
    boolean hasRespawnPoint = value.has("respawnPoint");

    if (!hasUuid) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'uuid'"));
    }

    if (!hasPoints) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'points'"));
    }

    if (!hasOpenedTreasureChests) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'openedTreasureChests'"));
    }

    if (!hasTimeRemainingSeconds) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'timeRemainingSeconds'"));
    }

    if (!hasVisitedCheckpoints) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'visitedCheckpoints'"));
    }

    if (!hasCurrentCheckpointName) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'currentCheckpointName'"));
    }

    if (!hasRaceStatus) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'raceStatus'"));
    }

    if (hasUuid) {
      try {
        playerData.uuid = UUID.fromString(value.getString("uuid"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'uuid' as UUID string: " + e.getMessage()));
      }
    }

    if (hasPoints) {
      try {
        playerData.points = value.getInt("points");
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'points' as integer: " + e.getMessage()));
      }
    }

    if (hasOpenedTreasureChests) {
      JSONArray openedTreasureChests = null;
      try {
        openedTreasureChests = value.getJSONArray("openedTreasureChests");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'openedTreasureChests' as array of UUID strings: "
                    + e.getMessage()));
      }

      if (openedTreasureChests != null) {
        ErrorPathContext openedTreasureChestsContext = context.extend("openedTreasureChests");
        for (int i = 0; i < openedTreasureChests.length(); i++) {
          try {
            playerData.openedTreasureChests.add(UUID.fromString(openedTreasureChests.getString(i)));
          } catch (Exception e) {
            errorReportBuilder.addDetail(
                new ErrorReportBuilder<ErrorPathContext>(
                        openedTreasureChestsContext.extend(Integer.toString(i)),
                        "Failed to parse UUID string")
                    .addDetail(new ErrorDetail(e.getMessage()))
                    .build());
          }
        }
      }
    }

    if (hasTimeRemainingSeconds) {
      try {
        playerData.timeRemainingSeconds = value.getInt("timeRemainingSeconds");
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'timeRemainingSeconds' as integer: " + e.getMessage()));
      }
    }

    if (hasVisitedCheckpoints) {
      try {
        JSONArray visitedCheckpoints = value.getJSONArray("visitedCheckpoints");
        for (int i = 0; i < visitedCheckpoints.length(); i++) {
          playerData.visitedCheckpoints.add(visitedCheckpoints.getString(i));
        }
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'visitedCheckpoints' as array of strings: " + e.getMessage()));
      }
    }

    if (hasCurrentCheckpointName) {
      try {
        playerData.currentCheckpointName = value.getString("currentCheckpointName");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'currentCheckpointName' as string: " + e.getMessage()));
      }
    }

    if (hasRaceStatus) {
      try {
        playerData.raceStatus = RaceStatus.valueOf(value.getString("raceStatus"));
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'raceStatus' as RaceStatus: " + e.getMessage()));
      }
    }

    if (hasRespawnPoint) {
      try {
        Result<Location, ErrorReport<ErrorPathContext>> jsonParseResult =
            JsonParser.parseLocationJson(context, value.getJSONObject("respawnPoint"));

        if (jsonParseResult.isError()) {
          errorReportBuilder.addDetail(jsonParseResult.getError());
        } else {
          playerData.respawnPoint = jsonParseResult.getValue();
        }
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'respawnPoint' as Location: " + e.getMessage()));
      }
    }

    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(playerData);
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("uuid", uuid.toString());
    jsonObject.put("points", points);
    jsonObject.put("openedTreasureChests", openedTreasureChests);
    jsonObject.put("timeRemainingSeconds", timeRemainingSeconds);
    jsonObject.put("visitedCheckpoints", visitedCheckpoints);
    jsonObject.put("currentCheckpointName", currentCheckpointName);
    jsonObject.put("raceStatus", raceStatus.name());
    if (respawnPoint != null) {
      jsonObject.put("respawnPoint", JsonParser.generateLocationJson(respawnPoint));
    }
    return jsonObject;
  }
}
