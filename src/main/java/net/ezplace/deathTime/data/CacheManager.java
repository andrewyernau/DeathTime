package net.ezplace.deathTime.data;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.tasks.BanTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getLogger;


public class CacheManager {
    private final DatabaseManager db;
    private final LoadingCache<UUID, Long> timersCache;
    private final Plugin plugin;
    private final BanTask banTask;
    private final BatchProcessor batchProcessor;
    private final CacheManager cacheManager;
    private final Set<UUID> bypassedPlayers = ConcurrentHashMap.newKeySet();

    public CacheManager(DatabaseManager db, DeathTime plugin) {
        this.db = db;
        this.plugin = plugin;
        cacheManager = this;
        this.banTask = new BanTask(plugin,cacheManager,db);
        this.batchProcessor = new BatchProcessor(db, plugin);

        this.timersCache = Caffeine.newBuilder()
                .maximumSize(SettingsManager.CACHE_SIZE)
                .expireAfterAccess(SettingsManager.CACHE_EXPIRATION, TimeUnit.MINUTES)
                .build(this::loadFromDatabase);
    }

    private Long loadFromDatabase(UUID uuid) {
        return db.getPlayerTime(uuid);
    }

    public void decrementAllTimers() {

        for (Map.Entry<UUID, Long> entry : timersCache.asMap().entrySet()) {
            UUID uuid = entry.getKey();
            if (hasBypass(uuid)) {
                continue;
            }

            long time = entry.getValue();

            if (time > 0) {
                long newTime = time - 1;
                timersCache.put(uuid, newTime);
                batchProcessor.addToBatch(uuid, newTime);

                if (newTime <= 0) {
                    Bukkit.getScheduler().runTask(plugin, () -> banTask.banPlayer(uuid));
                }
            }
        }
    }

    public void flushAllToDatabase() {
        timersCache.asMap().forEach(db::updatePlayerTime);
    }

    public Long getPlayerTime(UUID uuid) {
        Long time = timersCache.getIfPresent(uuid);
        if (time == null) {
            time = db.getPlayerTime(uuid); // Ddbb load
            if (time < 0) {
                time = (long) SettingsManager.INITIAL_TIME;
                db.updatePlayerTime(uuid, time);
            }
            timersCache.put(uuid, time);
        }
        return time;
    }

    /**
     * Integrity check because the cache might be inconsistent and end up with negative times.
     * */
    public void validateTimers() {
        for (UUID uuid : timersCache.asMap().keySet()) {
            long currentTime = timersCache.get(uuid);

            if (currentTime < 0) {
                getLogger().warning("Tiempo negativo detectado para " + uuid + ". Corrigiendo a 0.");
                timersCache.put(uuid, 0L);
                Bukkit.getScheduler().runTask(plugin, () -> banTask.banPlayer(uuid));
            }

            boolean isBanned = db.isPlayerBanned(uuid);
            if (isBanned && currentTime > 0) {
                getLogger().warning("Inconsistencia: Jugador " + uuid + " está baneado pero tiene tiempo positivo. Restableciendo.");
                db.updateBanStatus(uuid, 0);
                timersCache.put(uuid, 0L);
            } else if (!isBanned && currentTime <= 0) {
                getLogger().warning("Inconsistencia: Jugador " + uuid + " no está baneado pero tiene tiempo 0. Baneando.");
                Bukkit.getScheduler().runTask(plugin, () -> banTask.banPlayer(uuid));
            }
        }
    }

    public void updatePlayerTime(UUID uuid, long time) {
        if (time < 0) {
            time = 0;
            Bukkit.getScheduler().runTask(plugin, () -> banTask.banPlayer(uuid));
        }
        timersCache.put(uuid, time);
        batchProcessor.addToBatch(uuid, time);
    }

    public void invalidatePlayer(UUID uuid) {
        timersCache.invalidate(uuid);
    }

    public boolean hasBypass(UUID uuid) {
        if (bypassedPlayers.contains(uuid)) return true;
        return db.isBypass(uuid);
    }

    public void addBypass(UUID uuid) {
        bypassedPlayers.add(uuid);
        db.setBypass(uuid, true);
    }

    public void removeBypass(UUID uuid) {
        bypassedPlayers.remove(uuid);
        db.setBypass(uuid, false);
    }
}