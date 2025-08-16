package de.goodzclan.gooDzSky.listener;

import de.goodzclan.gooDzSky.GooDzSky;
import de.goodzclan.gooDzSky.battlepass.BattlePassLevel;
import de.goodzclan.gooDzSky.battlepass.BattlePassManager;
import de.goodzclan.gooDzSky.battlepass.PlayerBattlePassData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BattlePassGUIListener implements Listener {

    private final GooDzSky plugin;
    private final BattlePassManager battlePassManager;

    public BattlePassGUIListener(GooDzSky plugin, BattlePassManager battlePassManager) {
        this.plugin = plugin;
        this.battlePassManager = battlePassManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(battlePassManager.getGuiTitle())) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }
        ItemMeta meta = clickedItem.getItemMeta();

        if (meta.getPersistentDataContainer().has(GooDzSky.BATTLEPASS_ACTION_KEY, PersistentDataType.STRING)) {
            String action = meta.getPersistentDataContainer().get(GooDzSky.BATTLEPASS_ACTION_KEY, PersistentDataType.STRING);
            if ("LEVEL_UP".equals(action)) {
                handleLevelUp(player);
                plugin.getBattlePassCommand().openBattlePassGUI(player);
            }
            return;
        }

        if (meta.getPersistentDataContainer().has(GooDzSky.BATTLEPASS_LEVEL_KEY, PersistentDataType.INTEGER)) {
            int level = meta.getPersistentDataContainer().get(GooDzSky.BATTLEPASS_LEVEL_KEY, PersistentDataType.INTEGER);
            handleClaimReward(player, level);
            plugin.getBattlePassCommand().openBattlePassGUI(player);
        }
    }

    private void handleLevelUp(Player player) {
        plugin.getLogger().info("[DEBUG] handleLevelUp für " + player.getName() + " aufgerufen.");
        int cost = battlePassManager.getLevelUpCost();
        ItemStack costItem = new ItemStack(Material.DIAMOND_BLOCK, cost);

        if (player.getInventory().containsAtLeast(costItem, cost)) {
            player.getInventory().removeItem(costItem);
            PlayerBattlePassData data = plugin.getPlayerBattlePassData(player);

            plugin.getLogger().info("[DEBUG] Level von " + player.getName() + " VOR dem Level-Up: " + data.getCurrentLevel());
            data.levelUp();
            plugin.getLogger().info("[DEBUG] Level von " + player.getName() + " NACH dem Level-Up: " + data.getCurrentLevel());

            player.sendMessage("§aDu bist eine Stufe aufgestiegen! Du bist jetzt Stufe §e" + data.getCurrentLevel() + "§a.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        } else {
            player.sendMessage("§cDir fehlen Diamantblöcke, um aufzusteigen.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    private void handleClaimReward(Player player, int level) {
        PlayerBattlePassData data = plugin.getPlayerBattlePassData(player);
        BattlePassLevel bpLevel = battlePassManager.getLevels().get(level);
        if (bpLevel == null) return;

        if (data.canClaim(level)) {
            for (String command : bpLevel.getRewardCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
            data.claimLevel(level);
            player.sendMessage("§aBelohnungen für Stufe §e" + level + " §aerhalten!");
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1f);
        } else {
            player.sendMessage("§cDu kannst diese Belohnung nicht abholen.");
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
        }
    }
}