package uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.JsonDataStore;

public class PlayerTrackerDataStore extends JsonDataStore {
  private static PlayerTrackerDataStore instance;
  public Map<UUID, PlayerData> players = new HashMap<>();

  public static Result<Boolean, ErrorReport<ErrorPathContext>> initialize() {
    if (instance != null) {
      App.instance
          .getLogger()
          .warning("PlayerTrackerDataStore initialized more than once. This may be a bug.");
      return Result.ok(true);
    }

    instance = new PlayerTrackerDataStore();

    return instance.load();
  }

  public static PlayerTrackerDataStore getStore() {
    if (instance == null) {
      App.instance
          .getLogger()
          .severe(
              "PlayerTrackerDataStore accessed before initialization. This is an unrecoverable"
                  + " error. Shutting down server...");
      Bukkit.shutdown();
      return null;
    }
    return instance;
  }

  @Override
  protected Result<Boolean, ErrorReport<ErrorPathContext>> load() {
    App.instance.getLogger().info("Loading player data...");
    Result<Boolean, ErrorReport<ErrorPathContext>> result = super.load();

    if (result.isError()) {
      return result;
    }

    if (!data.has("players")) {
      ErrorPathContext ctx = new ErrorPathContext("<root>");
      return Result.error(
          new ErrorReportBuilder<>(
                  ctx, "Error loading treasure chest data from " + getDataFilePath())
              .addDetail(new ErrorDetail("Missing 'players' key"))
              .build());
    }

    ErrorPathContext playersKeyCtx = new ErrorPathContext("players");
    try {
      boolean error = false;
      ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
          new ErrorReportBuilder<>(playersKeyCtx, "Failed to load player data");

      JSONArray playersData = data.getJSONArray("players");

      for (int i = 0; i < playersData.length(); i++) {
        JSONObject playerData = playersData.getJSONObject(i);

        Result<PlayerData, ErrorReport<ErrorPathContext>> playerDataResult =
            PlayerData.fromJsonObject(playersKeyCtx.extend(String.valueOf(i)), playerData);

        if (playerDataResult.isError()) {
          error = true;
          errorReportBuilder.addDetail(playerDataResult.getError());
        } else {
          PlayerData playerDataValue = playerDataResult.getValue();
          playerDataValue.bindToPlayerTrackerDataStore(this);
          this.players.put(playerDataValue.uuid, playerDataValue);
        }
      }

      if (error) {
        return Result.error(errorReportBuilder.build());
      }
    } catch (Exception e) {
      return Result.error(
          new ErrorReport<>(playersKeyCtx, "Failed to load playerData: " + e.getMessage()));
    }

    return Result.ok(true);
  }

  @Override
  protected String getDataFileName() {
    return "playerData.json";
  }

  public Result<Boolean, String> reset() {
    players.clear();
    return savePlayerTracker();
  }

  public Result<Boolean, String> savePlayerTracker() {
    JSONArray players = new JSONArray();

    for (PlayerData playerData : this.players.values()) {
      players.put(playerData.toJsonObject());
    }

    data.put("players", players);

    Result<?, ErrorReport<ErrorPathContext>> result = save();

    if (result.isError()) {
      App.instance
          .getLogger()
          .warning(
              new ErrorReportBuilder<ErrorPathContext>(
                      new ErrorPathContext(getDataFileName()), "Failed to save player tracker data")
                  .addDetail(result.getError())
                  .build()
                  .pretty());
      return Result.error("Failed to save player tracker data. Please contact an administrator.");
    }

    return Result.ok(true);
  }

  public Result<PlayerData, String> getPlayerData(HumanEntity player) {
    PlayerData playerData = players.get(player.getUniqueId());

    if (playerData == null) {
      playerData = new PlayerData(player);
      playerData.bindToPlayerTrackerDataStore(this);
      players.put(player.getUniqueId(), playerData);
      Result<Boolean, String> saveResult = savePlayerTracker();

      if (saveResult.isError()) {
        return Result.error(saveResult.getError());
      }
    }

    return Result.ok(playerData);
  }
}
