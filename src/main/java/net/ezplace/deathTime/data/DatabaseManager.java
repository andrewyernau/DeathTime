package net.ezplace.deathTime.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ezplace.deathTime.config.SettingsManager;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    private final HikariDataSource dataSource;

    public DatabaseManager(File dataFolder) {
        HikariConfig config = new HikariConfig();

        switch (SettingsManager.DATABASE_TYPE.toLowerCase()) {
            case "h2":
                config.setJdbcUrl("jdbc:h2:" + dataFolder.getAbsolutePath() + "/" + SettingsManager.DATABASE_NAME);
                break;
            case "sqlite":
                config.setJdbcUrl("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/" + SettingsManager.DATABASE_NAME + ".db");
                break;
            case "mysql":
                config.setJdbcUrl("jdbc:mysql://" + SettingsManager.DATABASE_HOST + ":" + SettingsManager.DATABASE_PORT + "/" + SettingsManager.DATABASE_NAME);
                config.setUsername(SettingsManager.DATABASE_USERNAME);
                config.setPassword(SettingsManager.DATABASE_PASSWORD);
                break;
            case "postgresql":
                config.setJdbcUrl("jdbc:postgresql://" + SettingsManager.DATABASE_HOST + ":" + SettingsManager.DATABASE_PORT + "/" + SettingsManager.DATABASE_NAME);
                config.setUsername(SettingsManager.DATABASE_USERNAME);
                config.setPassword(SettingsManager.DATABASE_PASSWORD);
                break;
            default:
                throw new IllegalArgumentException("Tipo de base de datos no soportado: " + SettingsManager.DATABASE_TYPE);
        }

        config.setMaximumPoolSize(SettingsManager.DATABASE_POOLSIZE);
        config.setConnectionTimeout(SettingsManager.DATABASE_CONNTIMEOUT);
        dataSource = new HikariDataSource(config);

        createTables();
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS players (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "username VARCHAR(16) NOT NULL, " +
                             "timer BIGINT NOT NULL, " +
                             "banned_until BIGINT DEFAULT 0, " +
                             "bypass BOOLEAN DEFAULT FALSE)" )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getPlayerTime(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT timer FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong("timer") : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void updatePlayerTime(UUID uuid, long time) {
        String username = Bukkit.getPlayer(uuid) != null
                ? Bukkit.getPlayer(uuid).getName()
                : Bukkit.getOfflinePlayer(uuid).getName();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "MERGE INTO players(uuid, username, timer) KEY(uuid) VALUES(?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setLong(3, time);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBanStatus(UUID uuid, long bannedUntil) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET banned_until = ? WHERE uuid = ?")) {
            stmt.setLong(1, bannedUntil);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, Long> getActiveBans() {
        Map<UUID, Long> bans = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid, banned_until FROM players WHERE banned_until > 0")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                bans.put(uuid, rs.getLong("banned_until"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bans;
    }

    public boolean isPlayerBanned(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT banned_until FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getLong("banned_until") > System.currentTimeMillis();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setBypass(UUID uuid, boolean bypass) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE players SET bypass = ? WHERE uuid = ?")) {
            stmt.setBoolean(1, bypass);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBypass(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT bypass FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("bypass");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UUID getCachedUUID(String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid FROM players WHERE username = ?")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? UUID.fromString(rs.getString("uuid")) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Remove in the future hopefully. Should be useless hence I don't want to "unban" a player that has never logged in. (Maybe name change or something...)
    public UUID getUUIDFromMojang(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject json = new JSONObject(response.toString());
                String uuidStr = json.getString("id");
                return UUID.fromString(
                        uuidStr.replaceFirst(
                                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                "$1-$2-$3-$4-$5"
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}