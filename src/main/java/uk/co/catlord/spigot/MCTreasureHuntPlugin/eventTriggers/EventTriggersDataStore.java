package uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.JsonDataStore;

public class EventTriggersDataStore extends JsonDataStore {
  private static EventTriggersDataStore instance;
  public ArrayList<EventTrigger> eventTriggers = new ArrayList<>();

  public static Result<Boolean, ErrorReport<ErrorPathContext>> initialize() {
    if (instance != null) {
      App.instance
          .getLogger()
          .warning("EventTriggersDataStore initialized more than once. This may be a bug.");
      return Result.ok(true);
    }

    instance = new EventTriggersDataStore();

    return instance.load();
  }

  public static EventTriggersDataStore getStore() {
    if (instance == null) {
      App.instance
          .getLogger()
          .severe(
              "EventTriggersDataStore accessed before initialization. This is an unrecoverable"
                  + " error. Shutting down server...");
      Bukkit.shutdown();
      return null;
    }
    return instance;
  }

  @Override
  protected Result<Boolean, ErrorReport<ErrorPathContext>> load() {
    App.instance.getLogger().info("Loading event triggers...");
    Result<Boolean, ErrorReport<ErrorPathContext>> result = super.load();

    if (result.isError()) {
      return result;
    }

    if (!data.has("eventTriggers")) {
      ErrorPathContext ctx = new ErrorPathContext("<root>");
      return Result.error(
          new ErrorReportBuilder<>(
                  ctx, "Error loading event trigger data from " + getDataFilePath())
              .addDetail(new ErrorDetail("Missing 'eventTriggers' key"))
              .build());
    }

    try {
      boolean error = false;
      ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
          new ErrorReportBuilder<>(
              new ErrorPathContext("eventTriggers"),
              "Error loading event trigger data from " + getDataFilePath());

      JSONArray eventTriggersJson = data.getJSONArray("eventTriggers");

      for (int i = 0; i < eventTriggersJson.length(); i++) {
        JSONObject eventTriggerJson = eventTriggersJson.getJSONObject(i);
        Result<EventTrigger, ErrorReport<ErrorPathContext>> eventTriggerResult =
            EventTrigger.fromJsonObject(
                new ErrorPathContext("eventTriggers", String.valueOf(i)), eventTriggerJson);

        if (eventTriggerResult.isError()) {
          error = true;
          errorReportBuilder.addDetail(eventTriggerResult.getError());
        } else {
          eventTriggers.add(eventTriggerResult.getValue());
        }
      }

      if (error) {
        return Result.error(errorReportBuilder.build());
      }
    } catch (Exception e) {
      return Result.error(
          new ErrorReport<>(
              new ErrorPathContext("eventTriggers"),
              "Failed to load event triggers: " + e.getMessage()));
    }

    for (EventTrigger eventTrigger : eventTriggers) {
      eventTrigger.register();
    }

    return Result.ok(true);
  }

  @Override
  protected String getDataFileName() {
    return "eventTriggers.json";
  }
}
