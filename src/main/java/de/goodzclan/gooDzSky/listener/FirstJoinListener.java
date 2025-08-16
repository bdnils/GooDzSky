package de.goodzclan.gooDzSky.listener; // Ändere dies zu deinem Paketnamen

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FirstJoinListener implements Listener {

    private final JavaPlugin plugin;

    // Der Konstruktor ist nützlich, um auf die Hauptklasse des Plugins zuzugreifen (z.B. für den Logger)
    public FirstJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFirstJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        // hasPlayedBefore() gibt 'false' zurück, wenn der Spieler noch nie auf dem Server war.
        // Das '!' am Anfang kehrt den Wert um, die Bedingung ist also wahr, wenn der Spieler neu ist.
        if (!player.hasPlayedBefore()) {

            // --- HIER DEINE SPAWN-KOORDINATEN ANPASSEN ---

            // Hole die Welt, in der sich der Spawn befindet. "world" ist der Name der Hauptwelt.
            // Ändere dies zu "world_nether", "world_the_end" oder dem Namen deiner Custom-Welt.
            World spawnWorld = Bukkit.getWorld("SBLobby");

            if (spawnWorld == null) {
                plugin.getLogger().severe("Die Spawn-Welt 'world' konnte nicht gefunden werden! Teleport fehlgeschlagen.");
                return;
            }

            // Setze die genauen Koordinaten und die Blickrichtung
            double x = 30.50;   // X-Koordinate (Block-Mitte)
            double y = 95;    // Y-Koordinate (Höhe)
            double z = 90.5;  // Z-Koordinate (Block-Mitte)
            float yaw = 90.0f;  // Blickrichtung nach links/rechts (0=Süden, 90=Westen, 180=Norden, -90=Osten)
            float pitch = 0.0f; // Blickrichtung nach oben/unten

            Location spawnLocation = new Location(spawnWorld, x, y, z, yaw, pitch);

            // --- ENDE DER ANPASSUNG ---


            // Teleportiere den Spieler zur definierten Location
            player.teleport(spawnLocation);

            // Eine Nachricht an den Spieler und an die Konsole senden (optional, aber empfohlen)
            player.sendMessage("§aWillkommen auf dem Server! Du wurdest zum Spawn teleportiert.");
            plugin.getLogger().info("Der neue Spieler " + player.getName() + " wurde zum Spawn teleportiert.");
        }

        }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }
}