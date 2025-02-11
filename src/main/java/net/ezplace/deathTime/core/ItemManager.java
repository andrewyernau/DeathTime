package net.ezplace.deathTime.core;

import net.ezplace.deathTime.config.SettingsManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ItemManager {
    private final NamespacedKey key;

    public ItemManager(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "death_time_add");
    }

    public ItemStack createItem(int itemValue) {
        ItemStack item = new ItemStack(SettingsManager.ITEM_MATERIAL);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        meta.setDisplayName(SettingsManager.ITEM_NAME);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, Integer.toString(itemValue));
        item.setItemMeta(meta);

        return item;
    }
}