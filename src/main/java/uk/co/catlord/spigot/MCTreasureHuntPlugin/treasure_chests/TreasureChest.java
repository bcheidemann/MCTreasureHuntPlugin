package uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests;

import java.util.UUID;
import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class TreasureChest {
  public enum Type {
    TREASURE_CHEST
  }

  public UUID uuid;
  public String displayName;
  public Type type;
  public Location location;
  public String checkpointName;

  private TreasureChest() {}

  public TreasureChest(Location location, String checkpointName) {
    this.uuid = UUID.randomUUID();
    this.displayName = "Treasure Chest";
    this.type = Type.TREASURE_CHEST;
    this.location = location;
    this.checkpointName = checkpointName;
  }

  public static Result<TreasureChest, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    TreasureChest treasureChest = new TreasureChest();
    boolean error = false;
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse treasure chest");

    boolean hasUuid = value.has("uuid");
    boolean hasDisplayName = value.has("displayName");
    boolean hasType = value.has("type");
    boolean hasLocation = value.has("location");
    boolean hasCheckpointName = value.has("checkpointName");

    if (!hasUuid) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'uuid'"));
      error = true;
    }

    if (!hasDisplayName) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'displayName'"));
      error = true;
    }

    if (!hasType) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'type'"));
      error = true;
    }

    if (!hasLocation) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'location'"));
      error = true;
    }

    if (hasUuid) {
      try {
        treasureChest.uuid = UUID.fromString(value.getString("uuid"));
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'uuid' as UUID string: " + e.getMessage()));
        error = true;
      }
    }

    if (hasDisplayName) {
      try {
        treasureChest.displayName = value.getString("displayName");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'displayName' as string: " + e.getMessage()));
        error = true;
      }
    }

    if (hasType) {
      try {
        treasureChest.type = Type.valueOf(value.getString("type"));
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'type' as treasure chest type (TREASURE_CHEST): "
                    + e.getMessage()));
        error = true;
      }
    }

    if (hasLocation) {
      JSONObject locationJson;
      try {
        locationJson = value.getJSONObject("location");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'location' as JSON object: " + e.getMessage()));
        return Result.error(errorReportBuilder.build());
      }

      Result<Location, ErrorReport<ErrorPathContext>> locationParseResult =
          JsonParser.parseLocationJson(context.extend("location"), locationJson);

      if (locationParseResult.isError()) {
        errorReportBuilder.addDetail(locationParseResult.getError());
        return Result.error(errorReportBuilder.build());
      }

      treasureChest.location = locationParseResult.getValue();
    }

    if (hasCheckpointName) {
      try {
        treasureChest.checkpointName = value.getString("checkpointName");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'checkpointName' as string: " + e.getMessage()));
        error = true;
      }
    }

    if (error) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(treasureChest);
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("uuid", uuid.toString());
    jsonObject.put("displayName", displayName);
    jsonObject.put("type", type.toString());
    jsonObject.put("location", JsonParser.generateLocationJson(location));
    jsonObject.put("checkpointName", checkpointName);
    return jsonObject;
  }

  public boolean equals(TreasureChest treasureChest) {
    return treasureChest.location.equals(this.location);
  }
}
