package de.goodzclan.gooDzSky.listener;

import de.goodzclan.gooDzSky.lootboxes.LootReward;
import de.goodzclan.gooDzSky.lootboxes.LootboxType;
import de.goodzclan.gooDzSky.manager.LootboxManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerInteractListener implements Listener {

    private final LootboxManager lootboxManager;

    public PlayerInteractListener(LootboxManager lootboxManager) {
        this.lootboxManager = lootboxManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;

        Location loc = e.getClickedBlock().getLocation();

        if (lootboxManager.isLootboxLocation(loc)) {
            e.setCancelled(true);
            LootboxType lootbox = lootboxManager.getLootboxByLocation(loc);
            Player p = e.getPlayer();
            if (lootbox == null) return;

            ItemStack keyNeeded = lootbox.getKey();
            ItemStack keyInHand = e.getItem();

            boolean isTheRightKey = false;
// Prüfe, ob beide Items existieren, vom selben Typ sind und beide einen Namen haben
            if (keyInHand != null && keyNeeded.getType() == keyInHand.getType() && keyNeeded.hasItemMeta() && keyInHand.hasItemMeta()) {
                // Vergleiche die reinen Anzeigenamen als Text. Das ignoriert alle internen Datenunterschiede.
                if (keyNeeded.getItemMeta().getDisplayName().equals(keyInHand.getItemMeta().getDisplayName())) {
                    isTheRightKey = true;
                }
            }

            if (!isTheRightKey) {
                p.sendMessage("§cDu brauchst einen passenden Schlüssel!");
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                return;
            }

            // Schlüssel entfernen (einen davon)
            keyInHand.setAmount(keyInHand.getAmount() - 1);

            // Zufällige Belohnung ziehen
            LootReward reward = getRandomReward(lootbox.getRewards());
            p.getInventory().addItem(reward.getItem());

            p.sendMessage("§aDu hast gewonnen: §e" + reward.getItem().getAmount() + "x " + reward.getItem().getType());
            p.playSound(p.getLocation(), lootbox.getOpenSound(), 1f, 1f);
        }
    }

    private LootReward getRandomReward(List<LootReward> rewards) {
        double rand = Math.random();
        double cumulative = 0;
        for (LootReward reward : rewards) {
            cumulative += reward.getChance();
            if (rand <= cumulative) {
                return reward;
            }
        }
        return rewards.get(rewards.size() - 1);
    }
}
