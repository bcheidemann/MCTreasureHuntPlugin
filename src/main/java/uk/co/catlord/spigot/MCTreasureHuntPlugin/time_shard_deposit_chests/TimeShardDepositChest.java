package uk.co.catlord.spigot.MCTreasureHuntPlugin.time_shard_deposit_chests;

import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class TimeShardDepositChest {
  public Location location;

  public TimeShardDepositChest(Location location) {
    this.location = location;
  }

  public static Result<TimeShardDepositChest, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse time shard deposit chest");

    boolean hasLocation = value.has("location");

    if (!hasLocation) {
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'location'"));
    }

    Location location = null;
    if (hasLocation) {
      JSONObject locationJson = null;
      try {
        locationJson = value.getJSONObject("location");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'location' as JSON object: " + e.getMessage()));
      }

      if (locationJson != null) {
        Result<Location, ErrorReport<ErrorPathContext>> locationParseResult =
            JsonParser.parseLocationJson(context.extend("location"), locationJson);

        if (locationParseResult.isError()) {
          errorReportBuilder.addDetail(locationParseResult.getError());
        }

        location = locationParseResult.getValue();
      }
    }

    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(new TimeShardDepositChest(location));
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("location", JsonParser.generateLocationJson(location));
    return jsonObject;
  }
}
