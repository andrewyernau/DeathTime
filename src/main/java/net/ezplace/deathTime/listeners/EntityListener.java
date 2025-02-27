package net.ezplace.deathTime.listeners;

import net.ezplace.deathTime.config.SettingsManager;
import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.core.PlayerManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class EntityListener implements Listener {
    private final ItemManager itemManager;
    private final Random random = new Random();
    private final PlayerManager playerManager;

    public EntityListener(ItemManager itemManager, PlayerManager playerManager) {
        this.itemManager = itemManager;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        //Player drops
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player killer = victim.getKiller();
            //Prevent abuse
            if (killer != null && killer != victim) {
                if (playerManager.canDropItem(killer.getUniqueId(), victim.getUniqueId())) {
                    int timeToAdd = random.nextInt(60) + 30;
                    ItemStack timeItem = itemManager.createItem(timeToAdd);
                    victim.getWorld().dropItemNaturally(victim.getLocation(), timeItem);
                    playerManager.updateCooldown(killer.getUniqueId(), victim.getUniqueId());
                }
            }
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