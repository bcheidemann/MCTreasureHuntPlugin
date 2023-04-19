package uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints;

import org.bukkit.Location;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Shape3D;

public class Checkpoint {
  public String name;
  public Location location;
  public String previousCheckpointName;
  public Shape3D shape;

  private Checkpoint() {}

  public Checkpoint(String name, Location location, String previousCheckpointName, Shape3D shape) {
    this.name = name;
    this.location = location;
    this.previousCheckpointName = previousCheckpointName;
    this.shape = shape;
  }

  public static Result<Checkpoint, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    Checkpoint checkpoint = new Checkpoint();
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse checkpoint");

    // Validate that the JSON object has the required fields
    boolean hasLocation = value.has("location");
    boolean hasName = value.has("name");
    boolean hasPreviousCheckpointName = value.has("previousCheckpointName");
    boolean hasShape = value.has("shape");

    if (!hasLocation) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'location'"));
      return Result.error(errorReportBuilder.build());
    }

    if (!hasName) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'name'"));
      return Result.error(errorReportBuilder.build());
    }

    if (!hasPreviousCheckpointName) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'previousCheckpointName'"));
      return Result.error(errorReportBuilder.build());
    }

    if (!hasShape) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'shape'"));
      return Result.error(errorReportBuilder.build());
    }

    // Parse the JSON object
    if (hasLocation) {
      try {
        JSONObject locationJson = value.getJSONObject("location");

        Result<Location, ErrorReport<ErrorPathContext>> locationParseResult =
            JsonParser.parseLocationJson(context.extend("location"), locationJson);

        if (locationParseResult.isError()) {
          errorReportBuilder.addDetail(locationParseResult.getError());
        }

        checkpoint.location = locationParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'location' as JSON object: " + e.getMessage()));
      }
    }

    if (hasName) {
      try {
        checkpoint.name = value.getString("name");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'name' as string: " + e.getMessage()));
      }
    }

    if (hasPreviousCheckpointName) {
      try {
        checkpoint.previousCheckpointName = value.getString("previousCheckpointName");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail(
                "Failed to parse 'previousCheckpointName' as string: " + e.getMessage()));
      }
    }

    if (hasShape) {
      try {
        JSONObject shapeJson = value.getJSONObject("shape");

        Result<Shape3D, ErrorReport<ErrorPathContext>> shapeParseResult =
            Shape3D.fromJsonObject(context.extend("shape"), shapeJson);

        if (shapeParseResult.isError()) {
          errorReportBuilder.addDetail(shapeParseResult.getError());
        }

        checkpoint.shape = shapeParseResult.getValue();
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'shape' as JSON object: " + e.getMessage()));
      }
    }

    // Return the result
    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(checkpoint);
  }

  public JSONObject toJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("location", JsonParser.generateLocationJson(location));
    jsonObject.put("name", name);
    jsonObject.put("previousCheckpointName", previousCheckpointName);
    jsonObject.put("shape", shape.toJsonObject());
    return jsonObject;
  }
}
