package uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public class JsonParser {
  public static Result<Location, ErrorReport<ErrorPathContext>> parseLocation(
      ErrorPathContext context, JSONObject value) {
    boolean hasX = value.has("x");
    boolean hasY = value.has("y");
    boolean hasZ = value.has("z");
    boolean hasWorld = value.has("world");

    boolean error = false;
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse location");

    if (!hasX) {
      error = true;
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'x'"));
    }

    if (!hasY) {
      error = true;
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'y'"));
    }

    if (!hasZ) {
      error = true;
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'z'"));
    }

    if (!hasWorld) {
      error = true;
      errorReportBuilder.addDetail(new ErrorDetail("Missing key 'world'"));
    }

    double x = 0;
    if (hasX) {
      try {
        x = value.getDouble("x");
      } catch (Exception e) {
        error = true;
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse key 'x' as a number: " + e.getMessage()));
      }
    }

    double y = 0;
    if (hasY) {
      try {
        y = value.getDouble("y");
      } catch (Exception e) {
        error = true;
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse key 'y' as a number: " + e.getMessage()));
      }
    }

    double z = 0;
    if (hasZ) {
      try {
        z = value.getDouble("z");
      } catch (Exception e) {
        error = true;
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse key 'z' as a number: " + e.getMessage()));
      }
    }

    String worldName = "";
    if (hasWorld) {
      try {
        worldName = value.getString("world");
      } catch (Exception e) {
        error = true;
        errorReportBuilder
            .addDetail(new ErrorDetail("Failed to parse key 'world' as a string: " + e.getMessage()));
      }
    }

    if (error) {
      return Result.error(errorReportBuilder.build());
    }

    World world = Bukkit.getWorld(worldName);

    if (world == null) {
      return Result.error(errorReportBuilder
          .addDetail(new ErrorReport<ErrorPathContext>(context.extend("world"), "Failed to find world '" + worldName + "'")).build());
    }

    return Result.ok(new Location(world, x, y, z));
  }
}
