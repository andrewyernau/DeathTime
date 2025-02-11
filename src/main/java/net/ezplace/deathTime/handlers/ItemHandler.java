package net.ezplace.deathTime.handlers;

import net.ezplace.deathTime.utils.DeathTimeSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ItemHandler {
    private final NamespacedKey key;

    public ItemHandler(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "death_time_add");
    }

    public ItemStack createItem(int itemValue) {
        ItemStack item = new ItemStack(DeathTimeSettings.ITEM_MATERIAL);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(DeathTimeSettings.ITEM_NAME);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, Integer.toString(itemValue));
        item.setItemMeta(meta);

        return item;
    }
}