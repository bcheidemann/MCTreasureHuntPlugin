package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public abstract class JsonDataStore {
  protected abstract String getDataFileName();

  protected JSONObject data;

  protected String getDataFilePath() {
    return App.instance.getDataFolder().getAbsolutePath() + "/" + getDataFileName();
  }

  protected Result<Boolean, ErrorReport<ErrorPathContext>> load() {
    try {
      data = JsonFileLoader.loadJSONObject(getDataFilePath());
      return Result.ok(true);
    } catch (Exception error) {
      return Result.error(
          new ErrorReport<>(
              new ErrorPathContext(getDataFilePath()),
              "Failed to load JSON file: " + error.getMessage()));
    }
  }

  protected Result<Boolean, ErrorReport<ErrorPathContext>> save() {
    Result<?, Exception> result = JsonFileLoader.saveJSONObject(getDataFilePath(), data);

    if (result.isOk()) {
      return Result.ok(true);
    } else {
      return Result.error(
          new ErrorReport<>(
              new ErrorPathContext(getDataFilePath()),
              "Failed to save JSON file: " + result.getError().getMessage()));
    }
  }
}
