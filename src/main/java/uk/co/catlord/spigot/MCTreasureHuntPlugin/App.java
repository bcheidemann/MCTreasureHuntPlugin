package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("MCTreasureHuntPlugin is now enabled!");
        this.registerEventListeners();
    }

    private void registerEventListeners() {
        EventListener.register(this);
    }
}
