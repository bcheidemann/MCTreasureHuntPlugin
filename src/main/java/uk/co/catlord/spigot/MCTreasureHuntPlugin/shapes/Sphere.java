package uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes;

import org.bukkit.Location;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class Sphere extends Shape3D {
  private Location center;
  private float radius;

  public Sphere(Location center, float radius) {
    this.center = center;
    this.radius = radius;
  }

  @Override
  public boolean contains(Location point) {
    if (center.getWorld() != point.getWorld()) {
      return false;
    }

    return center.distanceSquared(point) <= radius * radius;
  }

  @Override
  public Location getCenter() {
    return center;
  }

  public static Result<Shape3D, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse sphere");

    // Validate that the JSON object has the required fields
    boolean hasCenter = value.has("center");
    boolean hasRadius = value.has("radius");

    if (!hasCenter) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'center'"));
      return Result.error(errorReportBuilder.build());
    }

    if (!hasRadius) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'radius'"));
    }

    // Parse the JSON object
    Location center = null;
    if (hasCenter) {
      try {
        JSONObject centerJson = value.getJSONObject("center");

        Result<Location, ErrorReport<ErrorPathContext>> centerParseResult =
            JsonParser.parseLocationJson(context.extend("center"), centerJson);

        if (centerParseResult.isError()) {
          errorReportBuilder.addDetail(centerParseResult.getError());
        }

        center = centerParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'center' as JSON object: " + e.getMessage()));
      }
    }

    float radius = 0;
    if (hasRadius) {
      try {
        radius = (float) value.getDouble("radius");
      } catch (JSONException e) {
        errorReportBuilder.addDetail(
            new ErrorReport<>(
                context.extend("radius"), "Failed to parse 'radius': " + e.getMessage()));
      }
    }

    // Return the result
    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(new Sphere(center, radius));
  }

  public JSONObject toJsonObject() {
    JSONObject result = new JSONObject();
    result.put("type", Type.SPHERE.name());
    result.put("radius", radius);
    result.put("center", JsonParser.generateLocationJson(center));
    return result;
  }
}
