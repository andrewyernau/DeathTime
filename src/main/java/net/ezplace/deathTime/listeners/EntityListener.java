package net.ezplace.deathTime.listeners;

import net.ezplace.deathTime.core.ItemManager;
import net.ezplace.deathTime.core.PlayerManager;
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
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player killer = victim.getKiller();
            if (killer != null && killer != victim) {
                if (playerManager.canDropItem(killer.getUniqueId(), victim.getUniqueId())) {
                    int timeToAdd = random.nextInt(60) + 30;
                    ItemStack timeItem = itemManager.createItem(timeToAdd);
                    victim.getWorld().dropItemNaturally(victim.getLocation(), timeItem);
                    playerManager.updateCooldown(killer.getUniqueId(), victim.getUniqueId());
                }
            }
        } else if (event.getEntityType() == EntityType.ENDER_DRAGON || event.getEntityType() == EntityType.WITHER) {
            int timeToAdd = random.nextInt(120) + 60;
            ItemStack timeItem = itemManager.createItem(timeToAdd);
            event.getDrops().add(timeItem);
        }
    }
}