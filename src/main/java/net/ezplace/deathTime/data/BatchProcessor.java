package net.ezplace.deathTime.data;

import net.ezplace.deathTime.DeathTime;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BatchProcessor {
    private final DatabaseManager dbManager;
    private final Map<UUID, Long> batchQueue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Plugin plugin;

    public BatchProcessor(DatabaseManager dbManager, Plugin plugin) {
        this.dbManager = dbManager;
        this.plugin = plugin;
        scheduler.scheduleAtFixedRate(this::flushBatch, 5, 5, TimeUnit.SECONDS);
    }

    public void addToBatch(UUID uuid, Long time) {
        try {
            batchQueue.put(uuid, time);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[ERROR] Failed to add to batch for user: " + uuid, e);
        }
    }

    public void flushBatch() {
        if (batchQueue.isEmpty()) return;

        batchQueue.forEach(dbManager::updatePlayerTime);
        batchQueue.clear();
    }

    public void flushSingle(UUID uuid) {
        Long time = batchQueue.get(uuid);
        if (time != null) {
            dbManager.updatePlayerTime(uuid, time);
            batchQueue.remove(uuid);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        flushBatch();
    }
}