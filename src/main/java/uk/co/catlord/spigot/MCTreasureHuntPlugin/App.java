package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.checkpoints.CheckpointDataStore;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.commands.SetTreasureChestCommand;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorPathContext;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.ErrorReport;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.treasure_chests.TreasureChestDataStore;

public class App extends JavaPlugin {
  public static App instance;

  @Override
  public void onEnable() {
    instance = this;
    if (!(register() && initialize())) {
      getLogger().warning("MCTreasureHuntPlugin failed to initialize. Disabling plugin...");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    getLogger().info("MCTreasureHuntPlugin is now enabled!");
  }

  private boolean register() {
    // Event Listeners
    EventListener.register(this);

    // Managers
    TreasureChestInventoryManager.register(this);

    // Commands
    new SetTreasureChestCommand().register(this);

    return true;
  }

  private boolean initialize() {
    boolean error = false;

    {
      Result<Boolean, ErrorReport<ErrorPathContext>> result = CheckpointDataStore.initialize();
      if (result.isError()) {
        getLogger().warning(result.getError().pretty());
        error = true;
      }
    }

    {
      Result<Boolean, ErrorReport<ErrorPathContext>> result = TreasureChestDataStore.initialize();
      if (result.isError()) {
        getLogger().warning(result.getError().pretty());
        error = true;
      }
    }

    return !error;
  }
}
