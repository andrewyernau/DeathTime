package net.ezplace.deathTime.other;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.data.CacheManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeathTimePlaceholders extends PlaceholderExpansion {
    private final DeathTime plugin;
    private final CacheManager cacheManager;

    public DeathTimePlaceholders(DeathTime plugin) {
        this.plugin = plugin;
        this.cacheManager = plugin.getCacheManager();
    }
    @Override
    public @NotNull String getIdentifier() {
        return "deathtime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "andrewyernau";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("time_left")) {
            if (player == null) return null;

            long timeLeft = cacheManager.getPlayerTime(player.getUniqueId());
            //(MM:SS or HH:MM:SS)
            return formatTime(timeLeft);
        }
        return null;
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "0";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}
