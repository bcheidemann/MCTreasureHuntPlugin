package uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.JsonDataStore;

public class TreasureChestDataStore extends JsonDataStore {
  private static TreasureChestDataStore instance;
  public ArrayList<TreasureChest> treasureChests = new ArrayList<>();

  public static Result<Boolean, ErrorReport<ErrorPathContext>> initialize() {
    if (instance != null) {
      App.instance
          .getLogger()
          .warning("TreasureChestDataStore initialized more than once. This may be a bug.");
      return Result.ok(true);
    }

    instance = new TreasureChestDataStore();

    return instance.load();
  }

  public static TreasureChestDataStore getStore() {
    if (instance == null) {
      App.instance
          .getLogger()
          .severe(
              "TreasureChestDataStore accessed before initialization. This is an unrecoverable"
                  + " error. Shutting down server...");
      Bukkit.shutdown();
      return null;
    }
    return instance;
  }

  @Override
  protected Result<Boolean, ErrorReport<ErrorPathContext>> load() {
    App.instance.getLogger().info("Loading treasure chests...");
    Result<Boolean, ErrorReport<ErrorPathContext>> result = super.load();

    if (result.isError()) {
      return result;
    }

    if (!data.has("treasureChests")) {
      ErrorPathContext ctx = new ErrorPathContext("<root>");
      return Result.error(
          new ErrorReportBuilder<>(
                  ctx, "Error loading treasure chest data from " + getDataFilePath())
              .addDetail(new ErrorDetail("Missing 'treasureChests' key"))
              .build());
    }

    try {
      boolean error = false;
      ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
          new ErrorReportBuilder<>(
              new ErrorPathContext("treasureChests"), "Failed to load treasure chests");

      JSONArray treasureChests = data.getJSONArray("treasureChests");

      for (int i = 0; i < treasureChests.length(); i++) {
        JSONObject treasureChest = treasureChests.getJSONObject(i);

        Result<TreasureChest, ErrorReport<ErrorPathContext>> treasureChestResult =
            TreasureChest.fromJsonObject(
                new ErrorPathContext("treasureChests", String.valueOf(i)), treasureChest);

        if (treasureChestResult.isError()) {
          error = true;
          errorReportBuilder.addDetail(treasureChestResult.getError());
        } else {
          this.treasureChests.add(treasureChestResult.getValue());
        }
      }

      if (error) {
        return Result.error(errorReportBuilder.build());
      }
    } catch (Exception e) {
      return Result.error(
          new ErrorReport<>(
              new ErrorPathContext("treasureChests"),
              "Failed to load treasureChests: " + e.getMessage()));
    }

    return Result.ok(true);
  }

  @Override
  protected String getDataFileName() {
    return "treasureChests.json";
  }

  public Result<Boolean, String> addTreasureChest(TreasureChest treasureChest) {
    if (isTreasureChestRegistered(treasureChest)) {
      return Result.error("Treasure chest already exists");
    }

    treasureChests.add(treasureChest);
    return saveTreasureChests();
  }

  public Result<Boolean, String> removeTreasureChest(Location location) {
    for (TreasureChest treasureChest : treasureChests) {
      if (treasureChest.location.equals(location)) {
        treasureChests.remove(treasureChest);
        return saveTreasureChests();
      }
    }

    return Result.error("Treasure chest not found");
  }

  private Result<Boolean, String> saveTreasureChests() {
    JSONArray treasureChests = new JSONArray();

    for (TreasureChest treasureChest : this.treasureChests) {
      treasureChests.put(treasureChest.toJsonObject());
    }

    data.put("treasureChests", treasureChests);

    Result<?, ErrorReport<ErrorPathContext>> result = save();

    if (result.isError()) {
      App.instance
          .getLogger()
          .warning(
              new ErrorReportBuilder<ErrorPathContext>(
                      new ErrorPathContext(getDataFileName()), "Failed to save treasure chests")
                  .addDetail(result.getError())
                  .build()
                  .pretty());
      return Result.error("Failed to save treasure chests. Please contact an administrator.");
    }

    return Result.ok(true);
  }

  public boolean isTreasureChestRegistered(TreasureChest treasureChest) {
    for (TreasureChest existingTreasureChest : this.treasureChests) {
      if (existingTreasureChest.equals(treasureChest)) {
        return true;
      }
    }

    return false;
  }

  public boolean isTreasureChestRegistered(Location location) {
    for (TreasureChest existingTreasureChest : this.treasureChests) {
      if (existingTreasureChest.location.equals(location)) {
        return true;
      }
    }

    return false;
  }

  public TreasureChest getTreasureChest(Location location) {
    for (TreasureChest existingTreasureChest : this.treasureChests) {
      if (existingTreasureChest.location.equals(location)) {
        return existingTreasureChest;
      }
    }

    return null;
  }

  public ArrayList<TreasureChest> getTreasureChestsForCheckpoint(String checkpointName) {
    ArrayList<TreasureChest> treasureChests = new ArrayList<>();

    for (TreasureChest treasureChest : this.treasureChests) {
      if (treasureChest.checkpointName != null && treasureChest.checkpointName.equals(checkpointName)) {
        treasureChests.add(treasureChest);
      }
    }

    return treasureChests;
  }
}
