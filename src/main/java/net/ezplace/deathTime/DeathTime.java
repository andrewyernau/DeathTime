package net.ezplace.deathTime;

import net.ezplace.deathTime.commands.DeathTimeCommands;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.data.BatchProcessor;
import net.ezplace.deathTime.data.CacheManager;
import net.ezplace.deathTime.data.DatabaseManager;
import net.ezplace.deathTime.listeners.PlayerListener;
import net.ezplace.deathTime.tasks.BanTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathTime extends JavaPlugin {

    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private BatchProcessor batchProcessor;
    private BanTask banTask;

    private static DeathTime instance;
    private ItemManager itemHandler;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            getLogger().severe("No se pudo cargar el controlador H2: " + e.getMessage());
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
            this.databaseManager = new DatabaseManager(getDataFolder());
            this.cacheManager = new CacheManager(databaseManager, this);
            this.batchProcessor = new BatchProcessor(databaseManager, this);
            this.banTask = new BanTask(this,cacheManager);


            DeathTimeCommands commandExecutor = new DeathTimeCommands(itemHandler, cacheManager);
            getCommand("deathtime").setExecutor(commandExecutor);
            getCommand("deathtime").setTabCompleter(commandExecutor);

            getServer().getPluginManager().registerEvents(new PlayerListener(cacheManager, batchProcessor, itemHandler), this);

            getLogger().info(MessagesManager.getInstance().getMessage("plugin.enabled"));

            startScheduledTasks();
        } catch (Exception e) {
            getLogger().severe("Error al inicializar el plugin: " + e.getMessage());
            e.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void startScheduledTasks() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Timers
            cacheManager.decrementAllTimers();

            // async batch processor
            getServer().getScheduler().runTaskAsynchronously(this, batchProcessor::flushBatch);
            getLogger().severe("Flushing batches. DEBUG!");
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
}
