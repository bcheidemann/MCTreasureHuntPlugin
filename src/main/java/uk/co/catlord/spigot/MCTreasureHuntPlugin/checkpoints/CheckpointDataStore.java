package uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorDetail;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.JsonDataStore;

public class CheckpointDataStore extends JsonDataStore implements Listener {
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

    register();

    return Result.ok(true);
  }

  @Override
  protected String getDataFileName() {
    return "checkpoints.json";
  }

  public Result<Boolean, String> addCheckpoint(Checkpoint checkpoint) {
    for (Checkpoint existingCheckpoint : checkpoints) {
      if (existingCheckpoint.name.equals(checkpoint.name)) {
        return Result.error("Checkpoint with name '" + checkpoint.name + "' already exists.");
      }
    }

    checkpoints.add(checkpoint);
    return saveCheckpoints();
  }

  private Result<Boolean, String> saveCheckpoints() {
    JSONArray checkpoints = new JSONArray();

    for (Checkpoint checkpoint : this.checkpoints) {
      checkpoints.put(checkpoint.toJsonObject());
    }

    data.put("checkpoints", checkpoints);

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

  public Checkpoint getCheckpointByName(String name) {
    for (Checkpoint checkpoint : checkpoints) {
      if (checkpoint.name.equals(name)) {
        return checkpoint;
      }
    }
    return null;
  }

  public List<String> getCheckpointNames() {
    List<String> names = new ArrayList<>();
    for (Checkpoint checkpoint : checkpoints) {
      names.add(checkpoint.name);
    }
    return names;
  }

  private void register() {
    App.instance.getServer().getPluginManager().registerEvents(this, App.instance);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    // The compass will not be updated until the player has joined. This even is fired before the
    // player
    // has properly joined the game so it seems to not update the compass target correctly.
    Bukkit.getScheduler()
        .runTaskLater(App.instance, () -> updateCompassForPlayer(event.getPlayer()), 1);
  }

  public void updateCompassForPlayer(Player player) {
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);

    if (playerDataResult.isError()) {
      player.sendMessage(
          "Failed to get player data (report this to an admin): " + playerDataResult.getError());
      return;
    }

    PlayerData playerData = playerDataResult.getValue();
    String checkpointName = playerData.getCurrentCheckpointName();

    if (checkpointName == "FINISH") {
      return;
    }

    Checkpoint nextCheckpoint = findNextCheckpoint(checkpointName);

    if (nextCheckpoint == null) {
      player.sendMessage(
          ChatColor.RED
              + "Failed to get the  checkpoint (report this to an admin): "
              + checkpointName);
      return;
    }

    player.setCompassTarget(nextCheckpoint.shape.getCenter());
  }

  public Checkpoint findNextCheckpoint(String currentCheckpointName) {
    for (Checkpoint checkpoint : checkpoints) {
      if (checkpoint.previousCheckpointName == null) {
        continue;
      }
      if (checkpoint.previousCheckpointName.equals(currentCheckpointName)) {
        return checkpoint;
      }
    }
    return null;
  }
}
