package net.ezplace.deathTime.listeners;

import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.core.PlayerManager;
import net.ezplace.deathTime.data.CacheManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;


public class EntityListener implements Listener {
    private final ItemManager itemManager;
    private final PlayerManager playerManager;
    private final CacheManager cacheManager;

    public EntityListener(ItemManager itemManager, PlayerManager playerManager, CacheManager cacheManager) {
        this.itemManager = itemManager;
        this.playerManager = playerManager;
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        //Player drops
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            String entityType = victim.getType().toString();
            int timeToAdd = SettingsManager.REWARDS.getOrDefault(entityType, 0);
            if (timeToAdd <= 0) return;
            Player killer = victim.getKiller();
            //Prevent abuse
            if (killer != null && killer != victim) {
                if (playerManager.canDropItem(killer.getUniqueId(), victim.getUniqueId())) {
                    ItemStack timeItem = itemManager.createItem(timeToAdd);
                    victim.getWorld().dropItemNaturally(victim.getLocation(), timeItem);
                    playerManager.updateCooldown(killer.getUniqueId(), victim.getUniqueId());
                }
            }

            cacheManager.updatePlayerTime(victim.getUniqueId(), SettingsManager.PLAYER_ON_DEATH_PENALTY);
        } //Entity drops
        else {
            Entity entity = event.getEntity();
            String entityType = entity.getType().toString();
            int timeToAdd = SettingsManager.REWARDS.getOrDefault(entityType, 0);

            if (timeToAdd <= 0) return;

            ItemStack timeItem = itemManager.createItem(timeToAdd);
            entity.getWorld().dropItemNaturally(entity.getLocation(), timeItem);

        }
    }
}