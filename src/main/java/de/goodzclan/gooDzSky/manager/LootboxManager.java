package de.goodzclan.gooDzSky.manager;

import de.goodzclan.gooDzSky.lootboxes.LootReward;
import de.goodzclan.gooDzSky.lootboxes.LootboxType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LootboxManager {

    private final Map<String, LootboxType> lootboxes = new HashMap<>();

    public void loadLootboxes(FileConfiguration config) {
        // Startnachricht
        Bukkit.getLogger().info("[GooDzSky] Lade Lootboxen aus der Konfiguration...");
        lootboxes.clear();

        ConfigurationSection section = config.getConfigurationSection("lootboxes");
        // Prüfung, ob der "lootboxes"-Abschnitt überhaupt existiert
        if (section == null) {
            Bukkit.getLogger().warning("[GooDzSky] Der 'lootboxes'-Abschnitt in der config.yml wurde nicht gefunden!");
            return;
        }

        // Schleife durch alle definierten Lootboxen (z.B. SMALL, BIG)
        for (String key : section.getKeys(false)) {
            String path = "lootboxes." + key + ".";

            try {
                // Schlüssel-Item laden
                ItemStack keyItem = createItem(
                        Material.valueOf(config.getString(path + "key.material")),
                        config.getString(path + "key.name")
                );

                // Belohnungen laden
                List<LootReward> rewards = new ArrayList<>();
                List<Map<?, ?>> rewardList = config.getMapList(path + "rewards");
                for (Map<?, ?> rewardData : rewardList) {
                    Material mat = Material.valueOf((String) ((Map<?, ?>) rewardData.get("item")).get("material"));
                    int amount = (int) ((Map<?, ?>) rewardData.get("item")).get("amount");
                    double chance = ((Number) rewardData.get("chance")).doubleValue();

                    rewards.add(new LootReward(new ItemStack(mat, amount), chance));
                }

                // Sound laden
                Sound sound = Sound.valueOf(config.getString(path + "sound_open"));

                // Location laden
                Location location = null;
                String locString = config.getString(path + "location");
                if (locString != null && !locString.isEmpty()) {
                    if (locString.contains(",")) {
                        String[] parts = locString.split(",");
                        if (parts.length == 4) {
                            World world = Bukkit.getWorld(parts[0].trim()); // .trim() entfernt Leerzeichen
                            // Wichtige Prüfung: Existiert die Welt?
                            if (world == null) {
                                Bukkit.getLogger().warning("[GooDzSky] Welt '" + parts[0] + "' für Lootbox '" + key + "' nicht gefunden! Diese Lootbox wird ohne Standort geladen.");
                            } else {
                                double x = Double.parseDouble(parts[1].trim());
                                double y = Double.parseDouble(parts[2].trim());
                                double z = Double.parseDouble(parts[3].trim());
                                location = new Location(world, x, y, z);
                            }
                        }
                    }
                } else {
                    Bukkit.getLogger().info("[GooDzSky] Lootbox '" + key + "' hat keinen Standort (location) in der config.");
                }

                lootboxes.put(key.toUpperCase(), new LootboxType(key, keyItem, rewards, sound, location));
                // Erfolgsmeldung für jede einzelne Kiste
                Bukkit.getLogger().info("[GooDzSky] Lootbox '" + key + "' erfolgreich geladen.");

            } catch (Exception e) {
                // Fehlermeldung, falls bei einer Kiste etwas schiefgeht (z.B. Material nicht gefunden)
                Bukkit.getLogger().log(Level.SEVERE, "[GooDzSky] Fehler beim Laden der Lootbox '" + key + "'. Bitte überprüfe die Konfiguration!", e);
            }
        }
        // Abschließende Zusammenfassung
        Bukkit.getLogger().info("[GooDzSky] " + lootboxes.size() + " Lootbox(en) wurden insgesamt geladen.");
    }

    public LootboxType getLootbox(String name) {
        return lootboxes.get(name.toUpperCase());
    }

    // NEUE, ZUVERLÄSSIGE VERSION
    public boolean isLootboxLocation(Location loc) {
        // Überprüft, ob die übergebene Location überhaupt gültig ist.
        if (loc == null || loc.getWorld() == null) {
            return false;
        }

        for (LootboxType type : lootboxes.values()) {
            Location lootboxLoc = type.getLocation();
            // Überprüft, ob die Lootbox-Location gültig ist.
            if (lootboxLoc == null || lootboxLoc.getWorld() == null) {
                continue; // Nächste Lootbox prüfen
            }

            // Der entscheidende Vergleich: Welt und die ganzzahligen Block-Koordinaten.
            if (lootboxLoc.getWorld().equals(loc.getWorld()) &&
                    lootboxLoc.getBlockX() == loc.getBlockX() &&
                    lootboxLoc.getBlockY() == loc.getBlockY() &&
                    lootboxLoc.getBlockZ() == loc.getBlockZ()) {
                return true; // Standort stimmt überein!
            }
        }
        return false;
    }

    public LootboxType getLootboxByLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }

        for (LootboxType type : lootboxes.values()) {
            Location lootboxLoc = type.getLocation();
            if (lootboxLoc == null || lootboxLoc.getWorld() == null) {
                continue;
            }

            if (lootboxLoc.getWorld().equals(loc.getWorld()) &&
                    lootboxLoc.getBlockX() == loc.getBlockX() &&
                    lootboxLoc.getBlockY() == loc.getBlockY() &&
                    lootboxLoc.getBlockZ() == loc.getBlockZ()) {
                return type; // Gibt die passende Lootbox zurück.
            }
        }
        return null;
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
