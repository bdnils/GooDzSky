package de.goodzclan.gooDzSky.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WarpManager {

    private final File file;
    private final FileConfiguration config;
    private final Map<String, Location> warps = new HashMap<>();

    public WarpManager(File dataFolder) {
        dataFolder.mkdirs();
        this.file = new File(dataFolder, "warps.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadWarps();
    }

    public void addWarp(String name, Location location) {
        warps.put(name.toLowerCase(), location);
        config.set("warps." + name.toLowerCase(), location.serialize());
        saveConfig();
    }

    public void removeWarp(String name) {
        warps.remove(name.toLowerCase());
        config.set("warps." + name.toLowerCase(), null);
        saveConfig();
    }

    public Location getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public Map<String, Location> getAllWarps() {
        return new HashMap<>(warps);
    }

    private void loadWarps() {
        if (config.contains("warps")) {
            for (String name : config.getConfigurationSection("warps").getKeys(false)) {
                warps.put(name.toLowerCase(), Location.deserialize(config.getConfigurationSection("warps." + name).getValues(false)));
            }
        }
    }

    private void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
