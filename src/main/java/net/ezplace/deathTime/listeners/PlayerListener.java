package net.ezplace.deathTime.listeners;

import net.ezplace.deathTime.config.MessagesManager;
import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.data.BatchProcessor;
import net.ezplace.deathTime.data.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class PlayerListener implements Listener {
    private final CacheManager cacheManager;
    private final BatchProcessor batchProcessor;
    private final ItemManager itemManager;

    public PlayerListener(CacheManager cacheManager, BatchProcessor batchProcessor, ItemManager itemManager) {
        this.cacheManager = cacheManager;
        this.batchProcessor = batchProcessor;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = cacheManager.getPlayerTime(uuid);

        // Load through cache/DB
        cacheManager.getPlayerTime(uuid);
        if (currentTime <= 0) {
            cacheManager.updatePlayerTime(uuid, SettingsManager.INITIAL_TIME);
            getLogger().info("Contador de " + player.getName() + " restablecido a " + SettingsManager.INITIAL_TIME + " segundos.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // Force save
        batchProcessor.flushSingle(uuid);
        cacheManager.invalidatePlayer(uuid);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        int timeToAdd = itemManager.getItemValue(item);
        if (timeToAdd > 0) {
            UUID uuid = player.getUniqueId();
            long currentTime = cacheManager.getPlayerTime(uuid);
            cacheManager.updatePlayerTime(uuid, currentTime + timeToAdd);
            item.setAmount(item.getAmount() - 1);
            player.sendMessage(MessagesManager.getInstance().getMessage("item.consume", Map.of("time", String.valueOf(timeToAdd))));
        }
    }
}