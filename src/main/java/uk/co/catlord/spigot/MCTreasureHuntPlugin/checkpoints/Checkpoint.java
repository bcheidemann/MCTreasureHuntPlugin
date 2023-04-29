package uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Shape3D;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;

public class Checkpoint implements Listener {
  public enum Type {
    CHECKPOINT,
    TREASURE_BEACON,
  }

  private CheckpointDataStore instance;

  public String name;
  public String previousCheckpointName = null;
  public Shape3D shape;
  public Type type = Type.CHECKPOINT;
  public String trailFrom = null;
  public Color color = null;
  public Location respawnPoint = null;

  private Checkpoint() {
    register();
  }

  public Checkpoint(
      String name,
      String previousCheckpointName,
      Shape3D shape,
      Type type,
      String trailFrom,
      Color color) {
    this.name = name;
    this.previousCheckpointName = previousCheckpointName;
    this.shape = shape;
    this.type = type;
    this.trailFrom = trailFrom;
    this.color = color;
    register();
  }

  public void bindToCheckpointDataStore(CheckpointDataStore instance) {
    this.instance = instance;
  }

  public static Result<Checkpoint, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    Checkpoint checkpoint = new Checkpoint();
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse checkpoint");

    // Validate that the JSON object has the required fields
    boolean hasName = value.has("name");
    boolean hasPreviousCheckpointName = value.has("previousCheckpointName");
    boolean hasShape = value.has("shape");
    boolean hasType = value.has("type");
    boolean hasTrailFrom = value.has("trailFrom");
    boolean hasColor = value.has("color");
    boolean hasRespawnPoint = value.has("respawnPoint");

    if (!hasName) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'name'"));
    }

    if (!hasShape) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'shape'"));
    }

    if (!hasType) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'type'"));
    }

    if (hasName) {
      try {
        checkpoint.name = value.getString("name");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'name' as string: " + e.getMessage()));
      }
    }

    if (hasPreviousCheckpointName) {
      try {
        checkpoint.previousCheckpointName = value.getString("previousCheckpointName");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'previousCheckpointName' as string: " + e.getMessage()));
      }
    }

    if (hasShape) {
      try {
        JSONObject shapeJson = value.getJSONObject("shape");

        Result<Shape3D, ErrorReport<ErrorPathContext>> shapeParseResult =
            Shape3D.fromJsonObject(context.extend("shape"), shapeJson);

        if (shapeParseResult.isError()) {
          errorReportBuilder.addDetail(shapeParseResult.getError());
        }

        checkpoint.shape = shapeParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'shape' as JSON object: " + e.getMessage()));
      }
    }

    if (hasType) {
      try {
        String typeString = value.getString("type");

        switch (typeString) {
          case "CHECKPOINT":
            checkpoint.type = Type.CHECKPOINT;
            break;
          case "TREASURE_BEACON":
            checkpoint.type = Type.TREASURE_BEACON;
            break;
          default:
            errorReportBuilder.addDetail(new ErrorDetail("Unknown checkpoint type: " + typeString));
        }
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'type' as string: " + e.getMessage()));
      }
    }

    if (hasTrailFrom) {
      try {
        checkpoint.trailFrom = value.getString("trailFrom");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'trailFrom' as string: " + e.getMessage()));
      }
    }

    if (hasColor) {
      try {
        checkpoint.color = Color.fromRGB(value.getInt("color"));
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'color' as integer: " + e.getMessage()));
      }
    }

    if (hasRespawnPoint) {
      try {
        JSONObject respawnPointJson = value.getJSONObject("respawnPoint");

        Result<Location, ErrorReport<ErrorPathContext>> respawnPointParseResult =
            JsonParser.parseLocationJson(context.extend("respawnPoint"), respawnPointJson);

        if (respawnPointParseResult.isError()) {
          errorReportBuilder.addDetail(respawnPointParseResult.getError());
        }

        checkpoint.respawnPoint = respawnPointParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'respawnPoint' as JSON object: " + e.getMessage()));
      }
    }

    // Return the result
    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(checkpoint);
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", name);
    jsonObject.put("previousCheckpointName", previousCheckpointName);
    jsonObject.put("shape", shape.toJsonObject());
    jsonObject.put("type", type.name());
    jsonObject.put("trailFrom", trailFrom);
    if (color != null) {
      jsonObject.put("color", color.asRGB());
    }
    if (respawnPoint != null) {
      jsonObject.put("respawnPoint", JsonParser.generateLocationJson(respawnPoint));
    }

    return jsonObject;
  }

  private void register() {
    App.instance.getServer().getPluginManager().registerEvents(this, App.instance);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    switch (type) {
      case CHECKPOINT:
        onPlayerMoveCheckpoint(event);
        break;
      case TREASURE_BEACON:
        onPlayerMoveTreasureBeacon(event);
        break;
    }
  }

  private void onPlayerMoveCheckpoint(PlayerMoveEvent event) {
    if (previousCheckpointName == null) {
      return;
    }

    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(event.getPlayer());

    if (playerDataResult.isError()) {
      event.getPlayer().sendMessage("Failed to get player data: " + playerDataResult.getError());
      return;
    }

    PlayerData playerData = playerDataResult.getValue();

    if (previousCheckpointName == null) {
      return;
    }

    if (!previousCheckpointName.equals(playerData.getCurrentCheckpointName())) {
      return;
    }

    if (!shape.contains(event.getTo())) {
      return;
    }

    if (shape.contains(event.getFrom())) {
      return;
    }

    if (respawnPoint != null) {
      Result<?, String> setRespawnPointResult = playerData.setRespawnPoint(respawnPoint);

      if (setRespawnPointResult.isError()) {
        event
            .getPlayer()
            .sendMessage(
                "Failed to set respawn point (contact an admin): "
                    + setRespawnPointResult.getError());
        return;
      } else {
        event
            .getPlayer()
            .sendMessage(
                ChatColor.GRAY
                    + "Your respawn point has been set "
                    + ChatColor.BOLD
                    + " ("
                    + name
                    + ")");
      }
    }

    Result<Boolean, String> visitCheckpointResult = playerData.visitCheckpoint(name);

    if (visitCheckpointResult.isError()) {
      event
          .getPlayer()
          .sendMessage("Failed to visit checkpoint: " + visitCheckpointResult.getError());
      return;
    }

    PlayerUtils.sendTitleToPlayer(
        event.getPlayer(), ChatColor.DARK_PURPLE + name, "Reached Checkpoint");

    CheckpointDataStore.getStore().updateCompassForPlayer(event.getPlayer());
  }

  private void onPlayerMoveTreasureBeacon(PlayerMoveEvent event) {
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(event.getPlayer());

    if (playerDataResult.isError()) {
      event.getPlayer().sendMessage("Failed to get player data: " + playerDataResult.getError());
      return;
    }

    PlayerData playerData = playerDataResult.getValue();

    if (trailFrom == null) {
      return;
    }

    if (!trailFrom.equals(playerData.getCurrentCheckpointName())) {
      return;
    }

    if (!shape.contains(event.getTo())) {
      return;
    }

    if (shape.contains(event.getFrom())) {
      return;
    }

    if (respawnPoint != null) {
      Result<?, String> setRespawnPointResult = playerData.setRespawnPoint(respawnPoint);

      if (setRespawnPointResult.isError()) {
        event
            .getPlayer()
            .sendMessage(
                "Failed to set respawn point (contact an admin): "
                    + setRespawnPointResult.getError());
        return;
      } else {
        event
            .getPlayer()
            .sendMessage(
                ChatColor.GRAY
                    + "Your respawn point has been set "
                    + ChatColor.BOLD
                    + " ("
                    + name
                    + ")");
      }
    }

    Result<Boolean, String> visitTreasureBeaconCheckpointResult =
        playerData.visitTreasureBeaconCheckpoint(name);

    if (visitTreasureBeaconCheckpointResult.isError()) {
      event
          .getPlayer()
          .sendMessage(
              "Failed to visit treasure beacon: " + visitTreasureBeaconCheckpointResult.getError());
      return;
    }

    PlayerUtils.sendTitleToPlayer(
        event.getPlayer(), ChatColor.DARK_PURPLE + name, "Reached Treasure Beacon");
  }

  public boolean isTreasureBeacon() {
    return type == Type.TREASURE_BEACON;
  }

  public Result<Boolean, String> setRespawnPoint(Location respawnPoint) {
    this.respawnPoint = respawnPoint;
    if (instance != null) {
      return this.instance.saveCheckpoints();
    }
    return Result.ok(true);
  }
}
