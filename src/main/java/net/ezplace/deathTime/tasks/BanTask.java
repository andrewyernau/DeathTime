package net.ezplace.deathTime.tasks;

import net.ezplace.deathTime.DeathTime;
import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;

import java.time.Duration;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class BanTask {

    private final Plugin plugin;

    public BanTask(DeathTime plugin) {
        this.plugin = plugin;
    }

    public void banPlayer(UUID uuid) {
        // Obtener el jugador
        Player player = Bukkit.getPlayer(uuid);

        // Si el jugador está online, expulsarlo
        if (player != null && player.isOnline()) {
            String banMessage = MessagesManager.getInstance().getMessage("ban.message");
            player.kickPlayer(banMessage);
        }

        // Banear al jugador temporalmente
        banPlayerTemporarily(uuid, Duration.ofMinutes(SettingsManager.BAN_DURATION));
    }

    public void banPlayerTemporarily(UUID uuid, Duration duration) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        // Obtener el PlayerProfile del jugador
        PlayerProfile playerProfile = offlinePlayer.getPlayerProfile();

        // Banear al jugador temporalmente
        banList.addBan(playerProfile, MessagesManager.getInstance().getMessage("ban.reason"), Instant.now().plus(duration), "DeathTimer");
    }

    public void checkExpiredBans() {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        // Iterar sobre las entradas de baneo
        for (Object obj : banList.getBanEntries()) {
            if (obj instanceof org.bukkit.BanEntry) {
                org.bukkit.BanEntry banEntry = (org.bukkit.BanEntry) obj;
                Date expirationDate = banEntry.getExpiration();

                if (expirationDate != null && expirationDate.before(new Date())) {
                    // El ban ha expirado, desbanear al jugador
                    String playerName = banEntry.getTarget();
                    banList.pardon(playerName);

                    // Notificar en consola
                    plugin.getLogger().info("Desbaneando a " + playerName + " (ban expirado).");
                }
            }
        }
    }

    public void unbanPlayer(UUID uuid) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        // Obtener el PlayerProfile del jugador
        PlayerProfile playerProfile = offlinePlayer.getPlayerProfile();

        // Desbanear al jugador
        banList.pardon(playerProfile);
    }
}
