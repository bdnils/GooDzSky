package de.goodzclan.gooDzSky.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpTask {

    private final Player player;
    private final Location warpLocation;
    private final String warpName; // Neuer Parameter: Warp-Name
    private final JavaPlugin plugin;
    private Location initialLocation;

    public WarpTask(Player player, Location warpLocation, String warpName, JavaPlugin plugin) {
        this.player = player;
        this.warpLocation = warpLocation;
        this.warpName = warpName; // Initialisiere den Warp-Namen
        this.plugin = plugin;
        this.initialLocation = player.getLocation();
    }

    public void start() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getLocation().distance(initialLocation) > 0.1) {
                player.sendMessage("§f[§6GooDzAttack§f] §cTeleportation abgebrochen! Du hast dich bewegt.");
                return;
            }
            player.teleport(warpLocation);
            player.sendMessage("§f[§6GooDzAttack§f] §aErfolgreich zu §e" + warpName + " §ateleportiert!"); // Ausgabe des Warp-Namens
        }, 100L); // 5 Sekunden (20 Ticks = 1 Sekunde)
    }
}

