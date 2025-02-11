package net.ezplace.deathTime.utils;

import net.ezplace.deathTime.DeathTime;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DeathTimeSettings {
    private final static DeathTimeSettings instance = new DeathTimeSettings();

    private File file;
    private YamlConfiguration config;

    public static String LANGUAGE;

    public static int INITIAL_TIME;
    public static int BAN_DURATION;

    public static Material ITEM_MATERIAL;
    public static String ITEM_NAME;
    public static String ITEM_LORE;

    private DeathTimeSettings(){

    }
    public static DeathTimeSettings getInstance() {
        return instance;
    }

    public void load(){
        file = new File(DeathTime.getInstance().getDataFolder(), "config.yml");

        if (!file.exists()){
            DeathTime.getInstance().saveResource("config.yml",false);
        }

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        LANGUAGE = String.valueOf(config.getString("Lang"));

        ITEM_MATERIAL = Material.valueOf(config.getString("Defaults.Item.Material"));
        ITEM_NAME = String.valueOf(config.getString("Defaults.Item.Name"));
        ITEM_LORE = String.valueOf(config.getString("Defaults.Item.Lore"));

    }

    public void save(){
        try{
            config.save(file);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void set(String path, Object value){
        config.set(path,value);
        save();
    }

}