package uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints;

import javax.annotation.Nullable;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Shape3D;

public class Checkpoint {
  public enum Type {
    CHECKPOINT,
    TREASURE_BEACON,
  }

  public String name;
  @Nullable public String previousCheckpointName = null;
  public Shape3D shape;
  public Type type = Type.CHECKPOINT;
  @Nullable public String trailFrom = null;

  private Checkpoint() {}

  public Checkpoint(
      String name, String previousCheckpointName, Shape3D shape, Type type, String trailFrom) {
    this.name = name;
    this.previousCheckpointName = previousCheckpointName;
    this.shape = shape;
    this.type = type;
    this.trailFrom = trailFrom;
  }

  public static Result<Checkpoint, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    Checkpoint checkpoint = new Checkpoint();
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse checkpoint");

    // Validate that the JSON object has the required fields
    boolean hasName = value.has("name");
    boolean hasPreviousCheckpointName = value.has("previousCheckpointName");
    boolean hasShape = value.has("shape");
    boolean hasType = value.has("type");
    boolean hasTrailFrom = value.has("trailFrom");

    if (!hasName) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'name'"));
    }

    if (!hasShape) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'shape'"));
    }

    if (!hasType) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'type'"));
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

    if (hasType) {
      try {
        String typeString = value.getString("type");

        switch (typeString) {
          case "CHECKPOINT":
            checkpoint.type = Type.CHECKPOINT;
            break;
          case "TREASURE_BEACON":
            checkpoint.type = Type.TREASURE_BEACON;
            break;
          default:
            errorReportBuilder.addDetail(new ErrorDetail("Unknown checkpoint type: " + typeString));
        }
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'type' as string: " + e.getMessage()));
      }
    }

    if (hasTrailFrom) {
      try {
        checkpoint.trailFrom = value.getString("trailFrom");
      } catch (Exception e) {
        errorReportBuilder.addDetail(
            new ErrorDetail("Failed to parse 'trailFrom' as string: " + e.getMessage()));
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
    jsonObject.put("name", name);
    jsonObject.put("previousCheckpointName", previousCheckpointName);
    jsonObject.put("shape", shape.toJsonObject());
    jsonObject.put("type", type.name());
    jsonObject.put("trailFrom", trailFrom);
    return jsonObject;
  }
}
