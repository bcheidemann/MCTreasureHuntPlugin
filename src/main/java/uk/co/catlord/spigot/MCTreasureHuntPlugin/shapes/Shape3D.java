package uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes;

import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public abstract class Shape3D {
  public enum Type {
    BOX,
    SPHERE,
    POINT,
  }

  public abstract boolean contains(Location pointLocation);

  public abstract Location getCenter();

  public static Result<Shape3D, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse shape");

    // Validate that the JSON object has the required fields
    boolean hasType = value.has("type");

    if (!hasType) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'type'"));
    }

    // Parse the JSON object
    if (hasType) {
      try {
        String type = value.getString("type");
        switch (type) {
          case "BOX":
            return Box.fromJsonObject(context, value);
          case "SPHERE":
            return Sphere.fromJsonObject(context, value);
          case "POINT":
            return Point.fromJsonObject(context, value);
          default:
            errorReportBuilder
                .addDetail(
                    new ErrorReport<>(context.extend("type"), "Invalid value for 'type': " + type))
                .build();
        }
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorReport<>(context.extend("type"), "Failed to parse 'type': " + e.getMessage()));
      }
    }

    return Result.error(errorReportBuilder.build());
  }

  public abstract JSONObject toJsonObject();
}
