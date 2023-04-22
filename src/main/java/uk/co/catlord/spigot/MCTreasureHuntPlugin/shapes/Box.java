package uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class Box extends Shape3D {
  private BoundingBox box;
  private World world;

  public Box(Location corner1, Location corner2) {
    double x1 = Math.min(corner1.getX(), corner2.getX());
    double y1 = Math.min(corner1.getY(), corner2.getY());
    double z1 = Math.min(corner1.getZ(), corner2.getZ());

    double x2 = Math.max(corner1.getX(), corner2.getX());
    double y2 = Math.max(corner1.getY(), corner2.getY());
    double z2 = Math.max(corner1.getZ(), corner2.getZ());

    this.box = new BoundingBox(x1, y1, z1, x2, y2, z2);
    this.world = corner1.getWorld();

    if (corner1.getWorld() != corner2.getWorld()) {
      throw new IllegalArgumentException("The two corners must be in the same world");
    }
  }

  @Override
  public boolean contains(Location point) {
    if (world != point.getWorld()) {
      return false;
    }

    return box.contains(point.getX(), point.getY(), point.getZ());
  }

  @Override
  public Location getCenter() {
    return new Location(world, box.getCenterX(), box.getCenterY(), box.getCenterZ());
  }

  public static Result<Shape3D, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse sphere");

    // Validate that the JSON object has the required fields
    boolean hasCorner1 = value.has("corner1");
    boolean hasCorner2 = value.has("corner2");

    if (!hasCorner1) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'corner1'"));
    }

    if (!hasCorner2) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'corner2'"));
    }

    // Parse the JSON object
    Location corner1 = null;
    if (hasCorner1) {
      try {
        JSONObject corner1Json = value.getJSONObject("corner1");

        Result<Location, ErrorReport<ErrorPathContext>> corner1ParseResult =
            JsonParser.parseLocationJson(context.extend("corner1"), corner1Json);

        if (corner1ParseResult.isError()) {
          errorReportBuilder.addDetail(corner1ParseResult.getError());
        }

        corner1 = corner1ParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'corner1' as JSON object: " + e.getMessage()));
      }
    }

    Location corner2 = null;
    if (hasCorner1) {
      try {
        JSONObject corner2Json = value.getJSONObject("corner2");

        Result<Location, ErrorReport<ErrorPathContext>> corner2ParseResult =
            JsonParser.parseLocationJson(context.extend("corner2"), corner2Json);

        if (corner2ParseResult.isError()) {
          errorReportBuilder.addDetail(corner2ParseResult.getError());
        }

        corner2 = corner2ParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'corner2' as JSON object: " + e.getMessage()));
      }
    }

    // Return the result
    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(new Box(corner1, corner2));
  }

  public JSONObject toJsonObject() {
    JSONObject result = new JSONObject();
    result.put("type", Type.BOX.name());
    result.put(
        "corner1",
        JsonParser.generateLocationJson(
            new Location(world, box.getMinX(), box.getMinY(), box.getMinZ())));
    result.put(
        "corner2",
        JsonParser.generateLocationJson(
            new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ())));
    return result;
  }
}
