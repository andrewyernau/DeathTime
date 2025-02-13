package net.ezplace.deathTime.data;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private final Cache<UUID, Long> cache;

    public CacheManager() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    public Long get(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    public void put(UUID uuid, Long time) {
        cache.put(uuid, time);
    }

    public void invalidate(UUID uuid) {
        cache.invalidate(uuid);
    }
}