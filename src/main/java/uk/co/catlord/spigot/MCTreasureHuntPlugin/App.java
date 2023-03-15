package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.commands.SetTreasureChestCommand;

public class App extends JavaPlugin {
  public static App instance;

  @Override
  public void onEnable() {
    instance = this;
    getLogger().info("MCTreasureHuntPlugin is now enabled!");
    register();
  }

  private void register() {
    // Event Listeners
    EventListener.register(this);

    // Managers
    TreasureChestManager.register(this);

    // Commands
    new SetTreasureChestCommand().register(this);
  }
}
