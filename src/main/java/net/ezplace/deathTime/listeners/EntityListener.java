package net.ezplace.deathTime.listeners;

import net.ezplace.deathTime.core.ItemManager;
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

    public EntityListener(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            // Jugador muerto
            Player player = (Player) event.getEntity();
            int timeToAdd = random.nextInt(60) + 30; // Entre 30 y 90 segundos
            ItemStack timeItem = itemManager.createItem(timeToAdd);
            player.getWorld().dropItemNaturally(player.getLocation(), timeItem);
        } else if (event.getEntityType() == EntityType.ENDER_DRAGON || event.getEntityType() == EntityType.WITHER) {
            // Boss muerto
            int timeToAdd = random.nextInt(120) + 60; // Entre 60 y 180 segundos
            ItemStack timeItem = itemManager.createItem(timeToAdd);
            event.getDrops().add(timeItem);
        }
    }
}