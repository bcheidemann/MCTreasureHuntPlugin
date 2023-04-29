package uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData.RaceStatus;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.BroadcastUtils;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.utils.PlayerUtils;

public abstract class EventTrigger {
  public enum EventTriggerType {
    COLLISION,
    INTERACT_BLOCK,
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
        errorReportBuilder.addDetail(
            new ErrorReport<>(context, "Invalid 'uuid': " + e.getMessage()));
      }
    }

    EventTriggerType type = null;
    if (hasType) {
      try {
        type = EventTriggerType.valueOf(value.getString("type"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorReport<>(context, "Invalid 'type': " + e.getMessage()));
      }
    }

    EventType event = null;
    if (hasEvent) {
      try {
        event = EventType.valueOf(value.getString("event"));
      } catch (IllegalArgumentException e) {
        errorReportBuilder.addDetail(
            new ErrorReport<>(context, "Invalid 'event': " + e.getMessage()));
      }
    }

    if (errorReportBuilder.hasErrors() || type == null) {
      return Result.error(errorReportBuilder.build());
    }

    EventTriggerData data = new EventTriggerData(uuid, type, event);

    switch (type) {
      case COLLISION:
        return CollisionEventTrigger.fromJsonObject(data, context, value);
      case INTERACT_BLOCK:
        return PlayerInteractBlockEventTrigger.fromJsonObject(data, context, value);
      default:
        return Result.error(new ErrorReport<>(context, "Unknown event trigger type"));
    }
  }

  protected void handleEvent(Player player) {
    switch (event) {
      case START:
        handleStartEvent(player);
        break;
      case FINISH:
        handleFinishEvent(player);
        break;
    }
  }

  protected void handleStartEvent(Player player) {
    PlayerData playerData = getPlayerData(player);
    if (playerData == null) {
      return;
    }
    RaceStatus status = playerData.getRaceStatus();
    if (status != RaceStatus.NOT_STARTED) {
      return;
    }
    Result<?, String> setRaceStatusResult = playerData.setRaceStatus(RaceStatus.STARTED);
    if (setRaceStatusResult.isError()) {
      player.sendMessage(setRaceStatusResult.getError());
      return;
    }
    PlayerUtils.sendTitleToPlayer(player, ChatColor.GREEN + "Go!", "Your time has started");
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1, 1);
    BroadcastUtils.broadcastRaceEvent(player.getDisplayName() + " has started!");
  }

  protected void handleFinishEvent(Player player) {
    PlayerData playerData = getPlayerData(player);
    if (playerData == null) {
      return;
    }
    RaceStatus status = playerData.getRaceStatus();
    if (status != RaceStatus.STARTED) {
      return;
    }
    Result<?, String> setRaceStatusResult = playerData.setRaceStatus(RaceStatus.FINISHED);
    if (setRaceStatusResult.isError()) {
      player.sendMessage(setRaceStatusResult.getError());
      return;
    }
    PlayerUtils.sendTitleToPlayer(player, ChatColor.GOLD + "Finished!", "You completed the hunt");
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1, 1);
    BroadcastUtils.broadcastRaceEvent(player.getDisplayName() + " has completed the hunt!");
    player.setGameMode(GameMode.SPECTATOR);
    PlayerUtils.celebration(player, true);
  }

  private PlayerData getPlayerData(Player player) {
    Result<PlayerData, String> playerData = PlayerTrackerDataStore.getStore().getPlayerData(player);
    if (playerData.isError()) {
      player.sendMessage(playerData.getError());
      return null;
    }

    return playerData.getValue();
  }
}
