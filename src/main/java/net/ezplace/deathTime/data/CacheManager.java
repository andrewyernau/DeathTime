package net.ezplace.deathTime.data;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.tasks.BanTask;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private final DatabaseManager db;
    private final LoadingCache<UUID, Long> timersCache;
    private final Plugin plugin;
    private final BanTask banTask;
    private final BatchProcessor batchProcessor;

    public CacheManager(DatabaseManager db, DeathTime plugin) {
        this.db = db;
        this.plugin = plugin;
        this.banTask = new BanTask(plugin);
        this.batchProcessor = new BatchProcessor(db, plugin);

        this.timersCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(this::loadFromDatabase);
    }

    private Long loadFromDatabase(UUID uuid) {
        return db.getPlayerTime(uuid);
    }

    public void decrementAllTimers() {
        timersCache.asMap().forEach((uuid, time) -> {
            long newTime = time - 1;
            if (newTime <= 0) {
                banTask.banPlayer(uuid);
            }
            timersCache.put(uuid, newTime);
            batchProcessor.addToBatch(uuid, newTime);
        });
    }

    public void flushAllToDatabase() {
        timersCache.asMap().forEach((uuid, time) -> {
            db.updatePlayerTime(uuid, time);
        });
    }

    public Long getPlayerTime(UUID uuid) {
        return timersCache.get(uuid);
    }

    public void updatePlayerTime(UUID uuid, long time) {
        timersCache.put(uuid, time);
        batchProcessor.addToBatch(uuid, time);
    }

    public void invalidatePlayer(UUID uuid) {
        timersCache.invalidate(uuid);
    }
}