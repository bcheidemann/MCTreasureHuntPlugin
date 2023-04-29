package uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReportBuilder;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.parsers.json.JsonParser;

public class PlayerInteractBlockEventTrigger extends EventTrigger implements Listener {
  private Location blockLocation;

  public PlayerInteractBlockEventTrigger(
      UUID uuid, EventTriggerType type, EventType event, Location blockLocation) {
    super(uuid, type, event);
    this.blockLocation = blockLocation;
  }

  public static Result<EventTrigger, ErrorReport<ErrorPathContext>> fromJsonObject(
      EventTriggerData data, ErrorPathContext context, JSONObject value) {
    ErrorReportBuilder<ErrorPathContext> errorReportBuilder =
        new ErrorReportBuilder<>(context, "Failed to parse checkpoint");

    // Validate that the JSON object has the required fields
    boolean hasBlockLocation = value.has("blockLocation");

    if (!hasBlockLocation) {
      errorReportBuilder.addDetail(new ErrorReport<>(context, "Missing 'blockLocation'"));
    }

    Location blockLocation = null;
    if (hasBlockLocation) {
      Result<Location, ErrorReport<ErrorPathContext>> blockLocationResult =
          JsonParser.parseLocationJson(
              context.extend("blockLocation"), value.getJSONObject("blockLocation"));
      if (blockLocationResult.isError()) {
        errorReportBuilder.addDetail(blockLocationResult.getError());
      } else {
        blockLocation = blockLocationResult.getValue();
      }
    }

    if (errorReportBuilder.hasErrors()) {
      return Result.error(errorReportBuilder.build());
    }

    return Result.ok(
        new PlayerInteractBlockEventTrigger(data.uuid, data.type, data.event, blockLocation));
  }

  public JSONObject toJsonObject() {
    return new JSONObject()
        .put("uuid", uuid.toString())
        .put("type", type.toString())
        .put("event", event.toString())
        .put("blockLocation", JsonParser.generateLocationJson(blockLocation));
  }

  public void register() {
    App.instance.getServer().getPluginManager().registerEvents(this, App.instance);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Block block = event.getClickedBlock();

    if (block == null) {
      return;
    }

    if (!blockLocation.equals(block.getLocation())) {
      return;
    }

    handleEvent(event.getPlayer());
  }
}
