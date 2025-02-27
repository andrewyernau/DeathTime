package net.ezplace.deathTime.tasks;

import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.data.CacheManager;
import net.ezplace.deathTime.data.DatabaseManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;

import java.time.Duration;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getServer;

public class BanTask {

    private final Plugin plugin;
    private final CacheManager cacheManager;
    private final DatabaseManager databaseManager;

    public BanTask(DeathTime plugin, CacheManager cacheManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
        this.databaseManager = databaseManager;
    }

    public void banPlayer(UUID uuid) {
        long banDurationMillis = SettingsManager.BAN_DURATION * 1000;
        long banUntil = System.currentTimeMillis() + banDurationMillis;
        databaseManager.updateBanStatus(uuid, banUntil);

        // Technically this does nothing, but I add it to make sure it is synced with the Database
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        getServer().broadcastMessage(MessagesManager.getInstance().getMessage("ban.broadcast",Map.of("user",offlinePlayer.getName())));
        if (offlinePlayer.isOnline()) {
            Player player = (Player) offlinePlayer;
            player.kickPlayer(MessagesManager.getInstance().getMessage("ban.message"));
        }
        Bukkit.getBanList(BanList.Type.NAME).addBan(offlinePlayer.getName(), MessagesManager.getInstance().getMessage("ban.outoftime"),
                Date.from(Instant.now().plusMillis(banDurationMillis)), null);
    }

    public void checkExpiredBans() {
        Map<UUID, Long> activeBans = databaseManager.getActiveBans();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> entry : activeBans.entrySet()) {
            UUID uuid = entry.getKey();
            long banUntil = entry.getValue();

            if (currentTime > banUntil) {
                // As before, the game unbans the user automatically, but I'm adding these lines of code to sync the database
                databaseManager.updateBanStatus(uuid, 0);
                cacheManager.updatePlayerTime(uuid, SettingsManager.INITIAL_TIME);
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
            }
        }
    }
}
