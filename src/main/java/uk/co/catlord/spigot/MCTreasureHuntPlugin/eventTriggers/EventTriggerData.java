package uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers;

import java.util.UUID;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers.EventTrigger.EventTriggerType;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.eventTriggers.EventTrigger.EventType;

public class EventTriggerData {
  public UUID uuid;
  public EventTriggerType type;
  public EventType event;

  public EventTriggerData(UUID uuid, EventTriggerType type, EventType event) {
    this.uuid = uuid;
    this.type = type;
    this.event = event;
  }
}
