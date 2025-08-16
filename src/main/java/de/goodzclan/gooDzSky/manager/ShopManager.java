package de.goodzclan.gooDzSky.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ShopManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    private String sellShopTitle;
    private String buyShopTitle;

    private final Map<Material, Double> sellPrices = new HashMap<>();
    private final Map<Material, Double> buyPrices = new HashMap<>();

    private ItemStack sellAllButton;
    private ItemStack switchToBuyButton;
    private ItemStack switchToSellButton;
    private ItemStack placeholderItem;


    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        config = plugin.getConfig();
        plugin.saveDefaultConfig(); // Erstellt die config.yml, falls sie nicht existiert

        // Titel laden
        sellShopTitle = ChatColor.translateAlternateColorCodes('&', config.getString("shop-titles.sell-gui", "&6&lVerkauf"));
        buyShopTitle = ChatColor.translateAlternateColorCodes('&', config.getString("shop-titles.buy-gui", "&a&lKauf"));

        // Preise laden
        loadPrices("sell-prices", sellPrices);
        loadPrices("buy-prices", buyPrices);

        // GUI Items laden
        sellAllButton = loadGuiItem("gui-items.sell-all-button");
        switchToBuyButton = loadGuiItem("gui-items.switch-to-buy-button");
        switchToSellButton = loadGuiItem("gui-items.switch-to-sell-button");

        // Platzhalter-Item erstellen
        placeholderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholderItem.getItemMeta();
        meta.setDisplayName(" ");
        placeholderItem.setItemMeta(meta);

        plugin.getLogger().info("Shop-Konfiguration erfolgreich geladen.");
    }

    private void loadPrices(String path, Map<Material, Double> priceMap) {
        priceMap.clear();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            plugin.getLogger().warning("Preis-Sektion '" + path + "' in config.yml nicht gefunden!");
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double price = section.getDouble(key);
                if (price > 0) {
                    priceMap.put(material, price);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ungültiges Material '" + key + "' in der Sektion '" + path + "' in config.yml.");
            }
        }
    }

    private ItemStack loadGuiItem(String path) {
        try {
            Material material = Material.valueOf(config.getString(path + ".material", "BARRIER"));
            String name = ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name", "&cFehler"));
            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList(path + ".lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
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
    public String getSellShopTitle() { return sellShopTitle; }
    public String getBuyShopTitle() { return buyShopTitle; }
    public Map<Material, Double> getSellPrices() { return sellPrices; }
    public Map<Material, Double> getBuyPrices() { return buyPrices; }
    public ItemStack getSellAllButton() { return sellAllButton.clone(); }
    public ItemStack getSwitchToBuyButton() { return switchToBuyButton.clone(); }
    public ItemStack getSwitchToSellButton() { return switchToSellButton.clone(); }
    public ItemStack getPlaceholderItem() { return placeholderItem.clone(); }
}