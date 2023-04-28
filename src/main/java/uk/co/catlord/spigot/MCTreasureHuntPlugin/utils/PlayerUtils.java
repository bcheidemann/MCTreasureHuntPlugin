package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerData;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.player_tracker.PlayerTrackerDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChest;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChestDataStore;

public class PlayerUtils {
  public static void givePlayerItemStack(HumanEntity player, ItemStack itemStack) {
    PlayerInventory inventory = player.getInventory();

    if (inventory.firstEmpty() == -1) {
      player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
    } else {
      inventory.addItem(itemStack);
    }
  }

  public static void givePlayerPoints(HumanEntity player, int points) {
    if (points == 0) {
      return;
    }

    Result<PlayerData, String> getPlayerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);

    if (getPlayerDataResult.isOk()) {
      Result<?, String> addPointsResult = getPlayerDataResult.getValue().addPoints(points);

      if (addPointsResult.isOk()) {
        if (points > 0) {
          StringBuilder message = new StringBuilder();
          message.append(ChatColor.GOLD);
          message.append("You have been awarded ");
          message.append(ChatColor.BOLD);
          message.append(points);
          message.append(ChatColor.RESET);
          message.append(ChatColor.GOLD);
          if (points == 1) {
            message.append(" point");
          } else {
            message.append(" points");
          }
          player.sendMessage(message.toString());
          celebration(player);
        } else {
          player.sendMessage("You have been deducted " + points + " points");
        }
      } else {
        player.sendMessage("Failed to give you points: " + addPointsResult.getError());
      }
    } else {
      player.sendMessage("Failed to give you points: " + getPlayerDataResult.getError());
    }
  }

  public static void celebration(HumanEntity player) {
    playCelebratorySound(player);

    if (hasRoofOverhead(player, 16)) {
      return;
    }

    Bukkit.getScheduler().runTaskLater(App.instance, () -> spawnCelebrationFireworks(player, 2), 5);
    Bukkit.getScheduler()
        .runTaskLater(App.instance, () -> spawnCelebrationFireworks(player, 1), 15);
    Bukkit.getScheduler()
        .runTaskLater(App.instance, () -> spawnCelebrationFireworks(player, 0), 20);
  }

  public static void spawnCelebrationFireworks(HumanEntity player, int power) {
    // Spawn the firework at the player's location
    Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);

    // Get the Firework's FireworkMeta
    FireworkMeta fireworkMeta = firework.getFireworkMeta();

    // Create a custom FireworkEffect with colors and effects
    FireworkEffect effect =
        FireworkEffect.builder()
            .flicker(true)
            .withColor(Color.RED)
            .withFade(Color.YELLOW)
            .with(FireworkEffect.Type.BURST)
            .trail(true)
            .build();

    // Add the custom FireworkEffect to the FireworkMeta
    fireworkMeta.addEffect(effect);

    // Set other properties, like the power (duration) of the firework
    fireworkMeta.setPower(power);

    // Apply the FireworkMeta to the Firework
    firework.setFireworkMeta(fireworkMeta);
  }

  public static void playCelebratorySound(HumanEntity player) {
    if (!(player instanceof Player)) {
      return;
    }
    Location playerLocation = player.getLocation();
    Sound sound = Sound.ENTITY_PLAYER_LEVELUP;
    float volume = 1.0f;
    float pitch = 1.0f;
    Player _player = (Player) player;
    _player.playSound(playerLocation, sound, volume, pitch);
  }

  public static boolean hasRoofOverhead(HumanEntity player, int heightLimit) {
    Location playerLocation = player.getLocation();
    World world = player.getWorld();

    int startX = playerLocation.getBlockX();
    int startY = playerLocation.getBlockY();
    int startZ = playerLocation.getBlockZ();

    for (int y = startY + 1; y <= startY + heightLimit; y++) {
      Block block = world.getBlockAt(startX, y, startZ);
      if (block.getType().isSolid()) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasPlayerOpenedTreasureChest(HumanEntity player, Block block) {
    TreasureChest treasureChest =
        TreasureChestDataStore.getStore().getTreasureChest(block.getLocation());
    if (treasureChest == null) {
      player.sendMessage(
          ChatColor.RED
              + "Treasure chest not found. Please report this issue to an administrator.");
      return false;
    }
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);
    if (playerDataResult.isError()) {
      player.sendMessage(ChatColor.RED + playerDataResult.getError());
      return false;
    }
    PlayerData playerData = playerDataResult.getValue();
    return playerData.hasOpenedTreasureChest(treasureChest.uuid);
  }

  public static void setPlayerOpenedTreasureChest(HumanEntity player, Block block) {
    TreasureChest treasureChest =
        TreasureChestDataStore.getStore().getTreasureChest(block.getLocation());
    if (treasureChest == null) {
      player.sendMessage(
          ChatColor.RED
              + "Treasure chest not found. Please report this issue to an administrator.");
      return;
    }
    Result<PlayerData, String> playerDataResult =
        PlayerTrackerDataStore.getStore().getPlayerData(player);
    if (playerDataResult.isError()) {
      player.sendMessage(ChatColor.RED + playerDataResult.getError());
      return;
    }
    PlayerData playerData = playerDataResult.getValue();
    playerData.addOpenedTreasureChest(treasureChest.uuid);
  }

  public static Location getLocationInfrontOfPlayer(HumanEntity player, double distance) {
    Location playerLocation = player.getLocation();
    Vector direction = playerLocation.getDirection();
    direction.setY(0);
    direction.normalize().multiply(distance);
    return playerLocation.add(direction);
  }

  public static void playSoundToAllPlayers(Sound sound, float volume, float pitch) {
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      player.playSound(player.getLocation(), sound, volume, pitch);
    }
  }

  public static void playSoundToAllPlayers(Sound sound) {
    playSoundToAllPlayers(sound, 1.0f, 1.0f);
  }

  public static void sendTitleToPlayer(
      Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
  }

  public static void sendTitleToPlayer(Player player, String title, String subtitle) {
    sendTitleToPlayer(player, title, subtitle, 10, 70, 20);
  }

  public static void sendTitleToAllPlayers(
      String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
  }

  public static void sendTitleToAllPlayers(String title, String subtitle) {
    sendTitleToAllPlayers(title, subtitle, 10, 70, 20);
  }
}
