package uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers;

import java.util.UUID;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.shapes.Shape3D;

public class CollisionEventTrigger extends EventTrigger {
  private Shape3D shape;

  public CollisionEventTrigger(UUID uuid, EventTriggerType type, EventType event, Shape3D shape) {
    super(uuid, type, event);
    this.shape = shape;
  }

  public static Result<EventTrigger, ErrorReport<ErrorPathContext>> fromJsonObject(
      EventTriggerData data, ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse checkpoint");

    // Validate that the JSON object has the required fields
    boolean hasShape = value.has("shape");

    if (!hasShape) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'shape'"));
    }

    Shape3D shape = null;
    if (hasShape) {
      Result<Shape3D, ErrorReport<ErrorPathContext>> shapeResult =
          Shape3D.fromJsonObject(context, value.getJSONObject("shape"));
      if (shapeResult.isError()) {
        errorReportBuilder.addDetail(shapeResult.getError());
      } else {
        shape = shapeResult.getValue();
      }
    }

    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(new CollisionEventTrigger(data.uuid, data.type, data.event, shape));
  }

  public JSONObject toJsonObject() {
    return new JSONObject()
        .put("uuid", uuid.toString())
        .put("type", type.toString())
        .put("event", event.toString())
        .put("shape", shape.toJsonObject());
  }
}
