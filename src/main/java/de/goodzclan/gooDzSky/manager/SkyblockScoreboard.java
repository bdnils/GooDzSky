package de.goodzclan.gooDzSky.manager;

import de.goodzclan.core.spigot.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SkyblockScoreboard {

    private final JavaPlugin plugin;

    public SkyblockScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
        startUpdater();
    }

    public void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = board.registerNewObjective("skyblock", "dummy",
                ChatColor.GOLD.toString() + ChatColor.BOLD + "Skyblock");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Zeilen – von oben nach unten sortiert (höchster Score zuerst)
        int line = 6;

        // Leerzeile
        obj.getScore(" ").setScore(line--);

        // Datum
        String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
        obj.getScore(ChatColor.GOLD + "Datum: " + ChatColor.WHITE + date).setScore(line--);

        // Coins
        PlayerData data = PlayerData.getPlayerDataByPlayer(player);
        BigInteger coins = (data != null) ? data.getCoins() : BigInteger.ZERO;
        obj.getScore(ChatColor.GOLD + "Münzen: " + ChatColor.WHITE + coins).setScore(line--);

        // RICHTIG: Alles wird in Sekunden umgerechnet
        // NEUER, KORREKTER CODE
// Annahme: data.getPlayTime() ist ebenfalls in Millisekunden gespeichert
        long currentSessionMillis = System.currentTimeMillis() - data.getJoinTime();
        long totalPlayTimeMillis = data.getPlayTime() + currentSessionMillis;

// WICHTIG: Millisekunden in Sekunden umrechnen
        long totalPlayTimeSeconds = totalPlayTimeMillis / 1000;

        obj.getScore(ChatColor.GOLD + "Spielzeit: " + ChatColor.WHITE + formatPlayTime(totalPlayTimeSeconds)).setScore(line--);

 // Noch eine Leerzeile
        obj.getScore("  ").setScore(line--);

        obj.getScore(ChatColor.GOLD + "TimeCraften.de").setScore(line--);


        player.setScoreboard(board);
    }
    public String formatPlayTime(long totalSeconds) {
        if (totalSeconds < 0) {
            return "Unbekannt";
        }

        // Die Berechnung bleibt unverändert
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        // Das Format wird angepasst:
        // %d für Stunden (ohne führende Null)
        // %02d für Minuten (mit führender Null, z.B. "05m")
        return String.format("%dh %02dm", hours, minutes);
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    setScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 5); // alle 5 Sekunden aktualisieren
    }
}
