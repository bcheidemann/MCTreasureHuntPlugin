package uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker;

import java.util.UUID;
import org.bukkit.entity.HumanEntity;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public class PlayerData {
  private PlayerTrackerDataStore store;
  public UUID uuid;
  private int points;

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

  public Result<Boolean, String> addPoints(int points) {
    this.points += points;
    return save();
  }

  public int getPoints() {
    return points;
  }

  public static Result<PlayerData, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    PlayerData playerData = new PlayerData();
    boolean error = false;
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse player data");

    boolean hasUuid = value.has("uuid");
    boolean hasPoints = value.has("points");

    if (!hasUuid) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'uuid'"));
      error = true;
    }

    if (!hasPoints) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'points'"));
      error = true;
    }

    if (hasUuid) {
      try {
        playerData.uuid = UUID.fromString(value.getString("uuid"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'uuid' as UUID string: " + e.getMessage()));
        error = true;
      }
    }

    if (hasPoints) {
      try {
        playerData.points = value.getInt("points");
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'points' as integer: " + e.getMessage()));
        error = true;
      }
    }

    if (error) {
      return Result.error(errorReportBuilder.build());
    } else {
      return Result.ok(playerData);
    }
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("uuid", uuid.toString());
    jsonObject.put("points", points);
    return jsonObject;
  }
}
