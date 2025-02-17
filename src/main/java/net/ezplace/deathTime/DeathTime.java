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
            // Cargar el controlador H2 manualmente
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            getLogger().severe("No se pudo cargar el controlador H2: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Plugin startup logic
        instance = this;
        this.itemHandler = new ItemManager(this);

        getLogger().info("\n" + "Version: 1.0.0");

        getDataFolder().mkdirs();

        SettingsManager.getInstance().load();
        MessagesManager.getInstance().loadMessages();

        try {
            // Inicializar componentes
            this.databaseManager = new DatabaseManager(getDataFolder());
            this.cacheManager = new CacheManager(databaseManager, this);
            this.batchProcessor = new BatchProcessor(databaseManager, this);
            this.banTask = new BanTask(this);

            // Registrar comandos y listeners
            DeathTimeCommands commandExecutor = new DeathTimeCommands(itemHandler, cacheManager);
            getCommand("deathtime").setExecutor(commandExecutor);
            getCommand("deathtime").setTabCompleter(commandExecutor);

            getServer().getPluginManager().registerEvents(new PlayerListener(cacheManager, batchProcessor, itemHandler), this);

            getLogger().info(MessagesManager.getInstance().getMessage("plugin.enabled"));

            // Iniciar tareas programadas
            startScheduledTasks();
        } catch (Exception e) {
            getLogger().severe("Error al inicializar el plugin: " + e.getMessage());
            e.printStackTrace();
            // Deshabilitar el plugin si hay un error crítico
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void startScheduledTasks() {
        // Timer principal (1 segundo)
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Lógica para decrementar tiempos
            cacheManager.decrementAllTimers();

            // Procesar batch cada 5 segundos (async)
            getServer().getScheduler().runTaskAsynchronously(this, batchProcessor::flushBatch);
        }, 0L, 20L); // 20 ticks = 1 segundo

        // Verificación de bans (cada minuto)
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // Lógica para verificar bans
            banTask.checkExpiredBans();
        }, 0L, 1200L); // 1200 ticks = 60 segundos
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        batchProcessor.shutdown();
        cacheManager.flushAllToDatabase();
    }

    public static DeathTime getInstance() {
        return instance;
    }
}
