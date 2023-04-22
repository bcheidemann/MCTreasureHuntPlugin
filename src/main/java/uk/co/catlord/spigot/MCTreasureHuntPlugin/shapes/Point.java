package uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes;

import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public class Point extends Shape3D {
  public Point() {}

  @Override
  public boolean contains(Location boxLocation, Location pointLocation) {
    return false;
  }

  public static Result<Shape3D, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    return Result.ok(new Point());
  }

  public JSONObject toJsonObject() {
    JSONObject result = new JSONObject();
    result.put("type", Type.POINT.name());
    return result;
  }
}
