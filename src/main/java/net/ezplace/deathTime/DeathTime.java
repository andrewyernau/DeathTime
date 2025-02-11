package net.ezplace.deathTime;

import net.ezplace.deathTime.commands.DeathTimeCommands;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathTime extends JavaPlugin {

    private static DeathTime instance;
    private static ItemManager itemHandler;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        this.itemHandler = new ItemManager(this);

        getLogger().info("\n"+ "Version: 1.0.0");

        getDataFolder().mkdirs();

        SettingsManager.getInstance().load();
        MessagesManager.getInstance().loadMessages();

        DeathTimeCommands commandExecutor = new DeathTimeCommands(itemHandler);
        getCommand("deathtime").setExecutor(commandExecutor);
        getCommand("deathtime").setTabCompleter(commandExecutor);
        getLogger().info(MessagesManager.getInstance().getMessage("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static DeathTime getInstance() {
        return instance;
    }
}
