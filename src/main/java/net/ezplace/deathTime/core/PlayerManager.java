package net.ezplace.deathTime.core;

import net.ezplace.deathTime.config.SettingsManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerManager {
    private final ConcurrentHashMap<String, AtomicLong> lastKillTimes = new ConcurrentHashMap<>();

    public boolean canDropItem(UUID killer, UUID victim) {
        String key = killer.toString() + "_" + victim.toString();
        long currentTime = System.currentTimeMillis() / 1000;
        AtomicLong lastTime = lastKillTimes.get(key);

        if (lastTime == null) {
            lastKillTimes.put(key, new AtomicLong(currentTime));
            return true;
        } else {
            long elapsed = currentTime - lastTime.get();
            return elapsed >= SettingsManager.KILL_COOLDOWN;
        }
    }

    public void updateCooldown(UUID killer, UUID victim) {
        String key = killer.toString() + "_" + victim.toString();
        lastKillTimes.put(key, new AtomicLong(System.currentTimeMillis() / 1000));
    }
}
