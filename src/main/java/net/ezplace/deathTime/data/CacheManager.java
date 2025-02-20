package net.ezplace.deathTime.data;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.tasks.BanTask;

import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getLogger;


public class CacheManager {
    private final DatabaseManager db;
    private final LoadingCache<UUID, Long> timersCache;
    private final Plugin plugin;
    private final BanTask banTask;
    private final BatchProcessor batchProcessor;
    private final CacheManager cacheManager;

    public CacheManager(DatabaseManager db, DeathTime plugin) {
        this.db = db;
        this.plugin = plugin;
        cacheManager = this;
        this.banTask = new BanTask(plugin,cacheManager);
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
            if (time > 0) {
                long newTime = time - 1;
                getLogger().info("Se ha decrementado el contador en 1 de " + uuid);
                getLogger().info("Tiempo restante de " + uuid + ": " + newTime);

                if (newTime <= 0) {
                    getLogger().info("El tiempo ha llegado a 0 para " + uuid);
                    banTask.banPlayer(uuid);
                }

                // Update cache
                timersCache.put(uuid, newTime);
                batchProcessor.addToBatch(uuid, newTime);
            }
        });
    }

    public void flushAllToDatabase() {
        timersCache.asMap().forEach(db::updatePlayerTime);
    }

    public Long getPlayerTime(UUID uuid) {
        Long time = timersCache.getIfPresent(uuid);
        if (time == null) {
            time = db.getPlayerTime(uuid); // Ddbb load
            if (time == -1) {
                time = (long) SettingsManager.INITIAL_TIME;
                db.updatePlayerTime(uuid, time);
            }
            timersCache.put(uuid, time);
        }
        getLogger().info("Tiempo obtenido para " + uuid + ": " + time);
        return time;
    }

    public void updatePlayerTime(UUID uuid, long time) {
        timersCache.put(uuid, time);
        batchProcessor.addToBatch(uuid, time);
    }

    public void invalidatePlayer(UUID uuid) {
        timersCache.invalidate(uuid);
    }
}