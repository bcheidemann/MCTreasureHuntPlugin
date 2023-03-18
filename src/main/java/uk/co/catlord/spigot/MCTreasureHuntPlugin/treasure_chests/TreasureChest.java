package uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests;

import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class TreasureChest {
  public Location location;

  private TreasureChest() {}

  public TreasureChest(Location location) {
    this.location = location;
  }

  public static Result<TreasureChest, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    TreasureChest treasureChest = new TreasureChest();
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse treasure chest");

    if (!value.has("location")) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'location'"));
      return Result.error(errorReportBuilder.build());
    }

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

    return Result.ok(treasureChest);
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("location", JsonParser.generateLocationJson(location));
    return jsonObject;
  }

  public boolean equals(TreasureChest treasureChest) {
    return treasureChest.location.equals(this.location);
  }
}
