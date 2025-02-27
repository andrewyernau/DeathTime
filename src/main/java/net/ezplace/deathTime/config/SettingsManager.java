package net.ezplace.deathTime.config;

import net.ezplace.deathTime.DeathTime;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SettingsManager {
    private final static SettingsManager instance = new SettingsManager();

    private File file;
    private YamlConfiguration config;

    public static String LANGUAGE;

    public static String DATABASE_TYPE;
    public static String DATABASE_HOST;
    public static int DATABASE_PORT;
    public static String DATABASE_NAME;
    public static String DATABASE_USERNAME;
    public static String DATABASE_PASSWORD;

    public static int DATABASE_POOLSIZE;
    public static int DATABASE_CONNTIMEOUT;

    public static Long INITIAL_TIME;
    public static Long BAN_DURATION;

    public static Material ITEM_MATERIAL;
    public static String ITEM_NAME;
    public static String ITEM_LORE;

    public static int KILL_COOLDOWN;

    public static Map<String, Integer> REWARDS = new HashMap<>();

    private SettingsManager(){

    }
    public static SettingsManager getInstance() {
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

        DATABASE_TYPE = config.getString("Database.type", "h2");
        DATABASE_HOST = config.getString("Database.host", "localhost");
        DATABASE_PORT = config.getInt("Database.port", 3306);
        DATABASE_NAME = config.getString("Database.database_name", "deathtimer");
        DATABASE_USERNAME = config.getString("Database.username", "root");
        DATABASE_PASSWORD = config.getString("Database.password", "password");

        DATABASE_POOLSIZE = config.getInt("Database.port", 3306);
        DATABASE_CONNTIMEOUT = config.getInt("Database.port", 3306);


        INITIAL_TIME = config.getLong("Defaults.Initial-time", 11340);
        BAN_DURATION = config.getLong("Defaults.Ban-duration",11340);
        ITEM_MATERIAL = Material.valueOf(config.getString("Defaults.Item.Material"));
        ITEM_NAME = String.valueOf(config.getString("Defaults.Item.Name"));
        ITEM_LORE = String.valueOf(config.getString("Defaults.Item.Lore"));

        KILL_COOLDOWN = config.getInt("Defaults.Kill-cooldown", 300);

        REWARDS.clear();
        ConfigurationSection rewardsSection = config.getConfigurationSection("Rewards.Entities");
        if (rewardsSection != null) {
            for (String entity : rewardsSection.getKeys(false)) {
                REWARDS.put(entity, rewardsSection.getInt(entity));
            }
        }
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