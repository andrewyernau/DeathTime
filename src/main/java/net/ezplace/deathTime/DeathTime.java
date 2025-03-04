package net.ezplace.deathTime;

import net.ezplace.deathTime.commands.DeathTimeCommands;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.core.PlayerManager;
import net.ezplace.deathTime.data.BatchProcessor;
import net.ezplace.deathTime.data.CacheManager;
import net.ezplace.deathTime.data.DatabaseManager;
import net.ezplace.deathTime.listeners.EntityListener;
import net.ezplace.deathTime.listeners.PlayerListener;
import net.ezplace.deathTime.other.DeathTimePlaceholders;
import net.ezplace.deathTime.tasks.BanTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathTime extends JavaPlugin {

    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private BatchProcessor batchProcessor;
    private BanTask banTask;

    private static DeathTime instance;
    private ItemManager itemHandler;

    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            getLogger().severe(MessagesManager.getInstance().getMessage("plugin.classnotfound.h2") + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;
        this.itemHandler = new ItemManager(this);

        getLogger().info("\n" + "Version: 1.0.0");

        getDataFolder().mkdirs();

        SettingsManager.getInstance().load();
        MessagesManager.getInstance().loadMessages();

        try {
            this.playerManager = new PlayerManager();
            this.databaseManager = new DatabaseManager(getDataFolder());
            this.cacheManager = new CacheManager(databaseManager, this);
            this.batchProcessor = new BatchProcessor(databaseManager, this);
            this.banTask = new BanTask(this,cacheManager,databaseManager);


            DeathTimeCommands commandExecutor = new DeathTimeCommands(itemHandler, cacheManager);
            getCommand("deathtime").setExecutor(commandExecutor);
            getCommand("deathtime").setTabCompleter(commandExecutor);

            getServer().getPluginManager().registerEvents(new PlayerListener(cacheManager, batchProcessor, itemHandler), this);
            getServer().getPluginManager().registerEvents(new EntityListener(itemHandler,playerManager,cacheManager),this);

            getLogger().info(MessagesManager.getInstance().getMessage("plugin.enabled"));

            startScheduledTasks();
        } catch (Exception e) {
            getLogger().severe(MessagesManager.getInstance().getMessage("plugin.disable") + e.getMessage());
            e.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DeathTimePlaceholders(this).register();
            getLogger().info(MessagesManager.getInstance().getMessage("plugin.enable.papi"));
        }
    }

    private void startScheduledTasks() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Timers
            cacheManager.decrementAllTimers();

            // Async batch processor
            getServer().getScheduler().runTaskAsynchronously(this, batchProcessor::flushBatch);
        }, 0L, 20L);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            banTask.checkExpiredBans();
        }, 0L, 1200L);
    }

    @Override
    public void onDisable() {
        batchProcessor.shutdown();
        cacheManager.flushAllToDatabase();
    }

    public static DeathTime getInstance() {
        return instance;
    }
    public CacheManager getCacheManager(){
        return cacheManager;
    }
}
