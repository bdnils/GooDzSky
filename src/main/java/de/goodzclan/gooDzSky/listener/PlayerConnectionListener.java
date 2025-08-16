package de.goodzclan.gooDzSky.listener;

import de.goodzclan.gooDzSky.GooDzSky;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final GooDzSky plugin;

    public PlayerConnectionListener(GooDzSky plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadPlayerBattlePassData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.unloadPlayerBattlePassData(event.getPlayer());
    }
}