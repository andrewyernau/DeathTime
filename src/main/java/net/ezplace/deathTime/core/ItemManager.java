package net.ezplace.deathTime.core;

import net.ezplace.deathTime.config.SettingsManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

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
        meta.setLore(Arrays.asList(
                "§7Añade §a" + itemValue + " segundos §7de vida.",
                "§8Usa este ítem para sobrevivir."
        ));

        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemValue);
        item.setItemMeta(meta);

        return item;
    }

    public int getItemValue(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            return 0;
        }

        return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }
}