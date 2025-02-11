package net.ezplace.deathTime.config;

import net.ezplace.deathTime.DeathTime;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {
    private static MessagesManager instance = new MessagesManager();
    private YamlConfiguration messages;
    private Map<String, String> messageCache = new HashMap<>();

    private MessagesManager() {}

    public static MessagesManager getInstance() {
        return instance;
    }

    public void loadMessages() {

        File langFolder = new File(DeathTime.getInstance().getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File file = new File(langFolder, SettingsManager.LANGUAGE + ".yml");

        if (!file.exists()) {
            DeathTime.getInstance().saveResource("lang/" + SettingsManager.LANGUAGE + ".yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
        messageCache.clear();

        messages.getKeys(true).forEach(key -> messageCache.put(key, messages.getString(key, key)));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messageCache.getOrDefault(key, key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message.replace("&", "§");
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }
}