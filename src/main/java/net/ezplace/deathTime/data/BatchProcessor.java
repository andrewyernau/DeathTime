package net.ezplace.deathTime.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BatchProcessor {
    private final DatabaseManager dbManager;
    private final Map<UUID, Long> batchQueue = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public BatchProcessor(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        scheduler.scheduleAtFixedRate(this::flushBatch, 5, 5, TimeUnit.SECONDS);
    }

    public void addToBatch(UUID uuid, Long time) {
        batchQueue.put(uuid, time);
    }

    private void flushBatch() {
        if (batchQueue.isEmpty()) return;

        batchQueue.forEach(dbManager::updatePlayerTime);
        batchQueue.clear();
    }

    public void shutdown() {
        scheduler.shutdown();
        flushBatch();
    }
}