package net.ezplace.deathTime.tasks;

import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.data.CacheManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;

import java.time.Duration;

import java.util.Date;
import java.util.UUID;

import static org.bukkit.Bukkit.broadcastMessage;

public class BanTask {

    private final Plugin plugin;
    private final CacheManager cacheManager;

    public BanTask(DeathTime plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    public void banPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        player.ban("", Duration.ofMinutes(SettingsManager.BAN_DURATION), MessagesManager.getInstance().getMessage("ban.reason"));
        String banMessage = MessagesManager.getInstance().getMessage("ban.message");
        player.kickPlayer(banMessage);
        broadcastMessage(MessagesManager.getInstance().getMessage("broadcast.ban"));

    }

    public void checkExpiredBans() {
        BanList<?> banList = Bukkit.getBanList(BanList.Type.NAME);

        // Iterate banned users
        for (Object obj : banList.getBanEntries()) {
            if (obj instanceof org.bukkit.BanEntry) {
                org.bukkit.BanEntry banEntry = (org.bukkit.BanEntry) obj;
                Date expirationDate = banEntry.getExpiration();

                if (expirationDate != null && expirationDate.before(new Date())) {

                    // Expired ban
                    String playerName = banEntry.getTarget();
                    banList.pardon(playerName); //Not necessary this line hence it does automatically.

                    // Reset
                    UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                    cacheManager.updatePlayerTime(uuid, SettingsManager.INITIAL_TIME);

                    // DEBUG
                    plugin.getLogger().info("Desbaneando a " + playerName + " y restableciendo su contador a " + SettingsManager.INITIAL_TIME + " segundos.");
                }
            }
        }
    }
}
