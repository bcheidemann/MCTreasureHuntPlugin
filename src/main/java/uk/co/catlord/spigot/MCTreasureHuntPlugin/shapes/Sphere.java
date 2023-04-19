package uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes;

import org.bukkit.Location;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public class Sphere extends Shape3D {
  private float radius;

  public Sphere(float radius) {
    this.radius = radius;
  }

  @Override
  public boolean contains(Location boxLocation, Location pointLocation) {
    if (boxLocation.getWorld() != pointLocation.getWorld()) {
      return false;
    }

    return boxLocation.distanceSquared(pointLocation) <= radius * radius;
  }

  public static Result<Shape3D, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse sphere");

    // Validate that the JSON object has the required fields
    boolean hasRadius = value.has("radius");

    if (!hasRadius) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'radius'"));
    }

    // Parse the JSON object
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

    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(new Sphere(radius));
  }

  public JSONObject toJsonObject() {
    JSONObject result = new JSONObject();
    result.put("type", Type.SPHERE.name());
    result.put("radius", radius);
    return result;
  }
}
