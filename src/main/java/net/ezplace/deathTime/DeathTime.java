package net.ezplace.deathTime;

import net.ezplace.deathTime.handlers.ItemHandler;
import net.ezplace.deathTime.utils.DeathTimeSettings;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathTime extends JavaPlugin {

    private static DeathTime instance;
    private static ItemHandler itemHandler;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        this.itemHandler = new ItemHandler(this);

        getLogger().info("\n"+ "Version: 1.0.0");

        getDataFolder().mkdirs();

        DeathTimeSettings.getInstance().load();
        LibreBuildMessages.getInstance().loadMessages();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static DeathTime getInstance() {
        return instance;
    }
}
