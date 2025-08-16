package de.goodzclan.gooDzSky.battlepass;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class BattlePassManager {

    private final JavaPlugin plugin;
    private final Map<Integer, BattlePassLevel> levels = new TreeMap<>();

    private String guiTitle;
    private int levelUpCost;
    private ItemStack levelUpButton;
    private ItemStack lockedLevelItem;
    private ItemStack unclaimedLevelItem;
    private ItemStack claimedLevelItem;

    public BattlePassManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File configFile = new File(plugin.getDataFolder(), "battlepass.yml");
        if (!configFile.exists()) {
            plugin.saveResource("battlepass.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        guiTitle = format(config.getString("settings.gui-title", "&8Battle Pass"));
        levelUpCost = config.getInt("settings.level-up-cost", 20);

        levelUpButton = loadGuiItem(config, "gui-items.level-up-button");
        lockedLevelItem = loadGuiItem(config, "gui-items.locked-level-item");
        unclaimedLevelItem = loadGuiItem(config, "gui-items.unclaimed-level-item");
        claimedLevelItem = loadGuiItem(config, "gui-items.claimed-level-item");

        levels.clear();
        ConfigurationSection levelsSection = config.getConfigurationSection("levels");
        if (levelsSection != null) {
            for (String key : levelsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);

                    // --- KORREKTUR WAR HIER ---
                    // Wir lesen jetzt relativ zur "levelsSection" mit dem 'key' als Pfad.
                    String name = format(levelsSection.getString(key + ".display-name"));
                    Material mat = Material.valueOf(levelsSection.getString(key + ".display-material"));
                    List<String> commands = levelsSection.getStringList(key + ".rewards");

                    levels.put(level, new BattlePassLevel(level, name, mat, commands));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden von Battle Pass Level '" + key + "' aus der battlepass.yml!", e);
                }
            }
        }
        plugin.getLogger().info(levels.size() + " Battle Pass Stufen geladen.");
    }

    // --- VERBESSERTE FORMAT-METHODE ---
    private String format(String text) {
        // Diese Prüfung verhindert den "Cannot translate null text"-Fehler
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private ItemStack loadGuiItem(FileConfiguration config, String path) {
        try {
            Material material = Material.valueOf(config.getString(path + ".material", "BARRIER"));
            String name = format(config.getString(path + ".name", "&cFehler"));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList(path + ".lore")) {
                lore.add(format(line));
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden des GUI-Items unter '" + path + "'", e);
            return new ItemStack(Material.BARRIER);
        }
    }

    // --- GETTERS ---
    public Map<Integer, BattlePassLevel> getLevels() { return levels; }
    public String getGuiTitle() { return guiTitle; }
    public int getLevelUpCost() { return levelUpCost; }
    public ItemStack getLevelUpButton() { return levelUpButton.clone(); }
    public ItemStack getLockedLevelItem() { return lockedLevelItem.clone(); }
    public ItemStack getUnclaimedLevelItem() { return unclaimedLevelItem.clone(); }
    public ItemStack getClaimedLevelItem() { return claimedLevelItem.clone(); }
}