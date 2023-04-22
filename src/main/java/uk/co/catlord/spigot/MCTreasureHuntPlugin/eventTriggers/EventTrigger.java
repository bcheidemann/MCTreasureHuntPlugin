package uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers;

import java.util.UUID;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public abstract class EventTrigger {
  public enum EventTriggerType {
    COLLISION,
  }

  public enum EventType {
    START,
    FINISH,
  }

  public UUID uuid;
  public EventTriggerType type;
  public EventType event;

  public EventTrigger(UUID uuid, EventTriggerType type, EventType event) {
    this.uuid = uuid;
    this.type = type;
    this.event = event;
  }

  public abstract JSONObject toJsonObject();

  public abstract void register();

  public static Result<EventTrigger, ErrorReport<ErrorPathContext>> fromJsonObject(
      ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse checkpoint");

    // Validate that the JSON object has the required fields
    boolean hasUuid = value.has("uuid");
    boolean hasType = value.has("type");
    boolean hasEvent = value.has("event");

    if (!hasUuid) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'uuid'"));
    }

    if (!hasType) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'type'"));
    }

    if (!hasEvent) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'event'"));
    }

    UUID uuid = null;
    if (hasUuid) {
      try {
        uuid = UUID.fromString(value.getString("uuid"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(new ErrorReport<>(context, "Invalid 'uuid'"));
      }
    }

    EventTriggerType type = null;
    if (hasType) {
      try {
        type = EventTriggerType.valueOf(value.getString("type"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(new ErrorReport<>(context, "Invalid 'type'"));
      }
    }

    EventType event = null;
    if (hasEvent) {
      try {
        event = EventType.valueOf(value.getString("event"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(new ErrorReport<>(context, "Invalid 'event'"));
      }
    }

    if (errorReportBuilder.hasErrors() || type == null) {
      return Result.error(errorReportBuilder.build());
    }

    EventTriggerData data = new EventTriggerData(uuid, type, event);

    switch (type) {
      case COLLISION:
        return CollisionEventTrigger.fromJsonObject(data, context, value);
      default:
        return Result.error(new ErrorReport<>(context, "Unknown event trigger type"));
    }
  }
}
