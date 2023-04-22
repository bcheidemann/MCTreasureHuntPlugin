package uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes;

import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class Point extends Shape3D {
  private Location location;

  public Point(Location location) {
    this.location = location;
  }

  @Override
  public boolean contains(Location pointLocation) {
    return false;
  }

  @Override
  public Location getCenter() {
    return location;
  }

  public static Result<Shape3D, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse Point");

    // Validate that the JSON object has the required fields
    boolean hasLocation = value.has("location");

    if (!hasLocation) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'location'"));
      return Result.error(errorReportBuilder.build());
    }

    // Parse the JSON object
    Location location = null;
    if (hasLocation) {
      try {
        JSONObject locationJson = value.getJSONObject("location");

        Result<Location, ErrorReport<ErrorPathContext>> locationParseResult =
            JsonParser.parseLocationJson(context.extend("location"), locationJson);

        if (locationParseResult.isError()) {
          errorReportBuilder.addDetail(locationParseResult.getError());
        }

        location = locationParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'location' as JSON object: " + e.getMessage()));
      }
    }

    // Return the result
    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(new Point(location));
  }

  public JSONObject toJsonObject() {
    JSONObject result = new JSONObject();
    result.put("type", Type.POINT.name());
    result.put("location", JsonParser.generateLocationJson(location));
    return result;
  }
}
