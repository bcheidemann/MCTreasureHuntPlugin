package uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints;

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

public class CheckpointDataStore extends JsonDataStore {
  private static CheckpointDataStore instance;
  public ArrayList<Checkpoint> checkpoints = new ArrayList<>();

  public static Result<Boolean, ErrorReport<ErrorPathContext>> initialize() {
    if (instance != null) {
      App.instance
          .getLogger()
          .warning("CheckpointDataStore initialized more than once. This may be a bug.");
      return Result.ok(true);
    }

    instance = new CheckpointDataStore();

    return instance.load();
  }

  public static CheckpointDataStore getStore() {
    if (instance == null) {
      App.instance
          .getLogger()
          .severe(
              "CheckpointDataStore accessed before initialization. This is an unrecoverable error."
                  + " Shutting down server...");
      Bukkit.shutdown();
      return null;
    }
    return instance;
  }

  @Override
  protected Result<Boolean, ErrorReport<ErrorPathContext>> load() {
    App.instance.getLogger().info("Loading checkpoints...");
    Result<Boolean, ErrorReport<ErrorPathContext>> result = super.load();

    if (result.isError()) {
      return result;
    }

    if (!data.has("checkpoints")) {
      ErrorPathContext ctx = new ErrorPathContext("<root>");
      return Result.error(
          new ErrorReportBuilder<>(ctx, "Error loading checkpoint data from " + getDataFilePath())
              .addDetail(new ErrorDetail("Missing 'checkpoints' key"))
              .build());
    }

    try {
      boolean error = false;
      ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
          new ErrorReportBuilder<>(
              new ErrorPathContext("checkpoints"), "Failed to load checkpoints");

      JSONArray checkpoints = data.getJSONArray("checkpoints");

      for (int i = 0; i < checkpoints.length(); i++) {
        JSONObject checkpoint = checkpoints.getJSONObject(i);

        Result<Checkpoint, ErrorReport<ErrorPathContext>> checkpointResult =
            Checkpoint.fromJsonObject(
                new ErrorPathContext("checkpoints", String.valueOf(i)), checkpoint);

        if (checkpointResult.isError()) {
          error = true;
          errorReportBuilder.addDetail(checkpointResult.getError());
        } else {
          this.checkpoints.add(checkpointResult.getValue());
        }
      }

      if (error) {
        return Result.error(errorReportBuilder.build());
      }
    } catch (Exception e) {
      return Result.error(
          new ErrorReport<>(
              new ErrorPathContext("checkpoints"),
              "Failed to load checkpoints: " + e.getMessage()));
    }

    return Result.ok(true);
  }

  @Override
  protected String getDataFileName() {
    return "checkpoints.json";
  }
}
