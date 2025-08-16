package de.goodzclan.gooDzSky.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.goodzclan.gooDzSky.GooDzSky;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class VoteListener implements Listener {

    private final GooDzSky plugin;

    public VoteListener(GooDzSky plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerVote(VotifierEvent event) {
        Vote vote = event.getVote();
        String playerName = vote.getUsername();

        // Finde den Spieler auf dem Server
        Player player = Bukkit.getPlayerExact(playerName);

        // Prüfe, ob der Spieler online ist
        if (player != null && player.isOnline()) {
            // Spieler ist online, gib ihm die Belohnung direkt
            ItemStack rewardItem = plugin.getVoteRewardItem();
            if (rewardItem == null) {
                plugin.getLogger().warning("Vote-Belohnung konnte nicht geladen werden (nicht in config.yml definiert?).");
                return;
            }

            // Prüfen, ob das Inventar voll ist
            if (player.getInventory().firstEmpty() == -1) {
                // Inventar ist voll, droppe das Item vor dem Spieler
                player.getWorld().dropItem(player.getLocation(), rewardItem);
                player.sendMessage(ChatColor.RED + "Dein Inventar war voll! Die Vote-Belohnung wurde vor dir auf den Boden fallen gelassen.");
            } else {
                // Gib das Item ins Inventar
                player.getInventory().addItem(rewardItem);
            }

            player.sendMessage(ChatColor.GREEN + "Vielen Dank für deinen Vote auf " + vote.getServiceName() + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            plugin.getLogger().info(playerName + " hat auf " + vote.getServiceName() + " gevotet und eine Belohnung erhalten.");

        } else {
            // Hinweis: Spieler war offline. Hier könnte man zukünftig Logik für eine Datenbank hinzufügen,
            // um die Belohnung zu speichern, bis der Spieler das nächste Mal online kommt.
            plugin.getLogger().info(playerName + " hat gevotet, war aber offline. Keine Belohnung vergeben.");
        }
    }
}