package uk.co.catlord.spigot.MCTreasureHuntPlugin.commands;

import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.App;

public abstract class RegisterableCommand implements CommandExecutor, TabCompleter {
  protected App instance;

  protected abstract String getName();

  protected Server getServer() {
    return instance.getServer();
  }

  public void register(App instance) {
    this.instance = instance;
    instance.getCommand(getName()).setExecutor(this);
  }
}
