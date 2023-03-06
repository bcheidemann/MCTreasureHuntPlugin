package uk.co.catlord.spigot.MCTreasureHuntPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    public static App instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("MCTreasureHuntPlugin is now enabled!");
        register();
    }

    private void register() {
        EventListener.register(this);
        TreasureChestManager.register(this);
    }
}
