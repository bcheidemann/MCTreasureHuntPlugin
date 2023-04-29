package uk.co.catlord.spigot.MCTreasureHuntPlugin.time_shard_deposit_chests;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.JsonDataStore;

public class TimeShardDepositChestDataStore extends JsonDataStore {
  private static TimeShardDepositChestDataStore instance;
  public List<TimeShardDepositChest> timeShardDepositChests = new ArrayList<>();

  public static Result<Boolean, ErrorReport<ErrorPathContext>> initialize() {
    if (instance != null) {
      App.instance
          .getLogger()
          .warning("TimeShardDepositChestDataStore initialized more than once. This may be a bug.");
      return Result.ok(true);
    }

    instance = new TimeShardDepositChestDataStore();

    return instance.load();
  }

  public static TimeShardDepositChestDataStore getStore() {
    if (instance == null) {
      App.instance
          .getLogger()
          .severe(
              "TimeShardDepositChestDataStore accessed before initialization. This is an"
                  + " unrecoverable error. Shutting down server...");
      Bukkit.shutdown();
      return null;
    }
    return instance;
  }

  @Override
  protected Result<Boolean, ErrorReport<ErrorPathContext>> load() {
    App.instance.getLogger().info("Loading time shard deposit chests...");
    Result<Boolean, ErrorReport<ErrorPathContext>> result = super.load();

    if (result.isError()) {
      return result;
    }

    if (!data.has("timeShardDepositChests")) {
      ErrorPathContext ctx = new ErrorPathContext("<root>");
      return Result.error(
          new ErrorReport<>(ctx, "Missing 'timeShardDepositChests' key" + getDataFilePath()));
    }

    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(
            new ErrorPathContext("timeShardDepositChests"), "Failed to load treasure chests");
    try {

      JSONArray timeShardDepositChests = data.getJSONArray("timeShardDepositChests");
      ErrorPathContext ctx = new ErrorPathContext("timeShardDepositChests");

      for (int i = 0; i < timeShardDepositChests.length(); i++) {
        JSONObject timeShardDepositChest = timeShardDepositChests.getJSONObject(i);

        Result<TimeShardDepositChest, ErrorReport<ErrorPathContext>> timeShardDepositChestResult =
            TimeShardDepositChest.fromJsonObject(
                ctx.extend(String.valueOf(i)), timeShardDepositChest);

        if (timeShardDepositChestResult.isError()) {
          errorReportBuilder.addDetail(timeShardDepositChestResult.getError());
          continue;
        }

        this.timeShardDepositChests.add(timeShardDepositChestResult.getValue());
      }
    } catch (Exception e) {
      return Result.error(
          new ErrorReport<>(
              new ErrorPathContext("timeShardDepositChests"),
              "Failed to load timeShardDepositChests: " + e.getMessage()));
    }

    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(true);
  }

  @Override
  protected String getDataFileName() {
    return "timeShardDepositChests.json";
  }

  public boolean isTimeShardDepositChestRegisteredAt(Location location) {
    for (TimeShardDepositChest timeShardDepositChest : timeShardDepositChests) {
      if (timeShardDepositChest.location.equals(location)) {
        return true;
      }
    }
    return false;
  }

  public Result<Boolean, String> addTimeShardDepositChest(
      TimeShardDepositChest timeShardDepositChest) {
    if (isTimeShardDepositChestRegisteredAt(timeShardDepositChest.location)) {
      return Result.error("A time shard deposit chest is already registered at this location");
    }

    timeShardDepositChests.add(timeShardDepositChest);
    return saveTimeShardDepositChests();
  }

  private Result<Boolean, String> saveTimeShardDepositChests() {
    JSONArray timeShardDepositChests = new JSONArray();

    for (TimeShardDepositChest timeShardDepositChest : this.timeShardDepositChests) {
      timeShardDepositChests.put(timeShardDepositChest.toJsonObject());
    }

    data.put("timtimeShardDepositChestseShardDepositChests", timeShardDepositChests);

    Result<?, ErrorReport<ErrorPathContext>> result = save();

    if (result.isError()) {
      App.instance
          .getLogger()
          .warning(
              new ErrorReportBuilder<ErrorPathContext>(
                      new ErrorPathContext(getDataFileName()),
                      "Failed to save time shard deposit chests")
                  .addDetail(result.getError())
                  .build()
                  .pretty());
      return Result.error(
          "Failed to save time shard deposit chests. Please contact an administrator.");
    }

    return Result.ok(true);
  }
}
