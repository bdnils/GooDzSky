package de.goodzclan.gooDzSky.commands;

import de.goodzclan.gooDzSky.GooDzSky;
import de.goodzclan.gooDzSky.battlepass.BattlePassLevel;
import de.goodzclan.gooDzSky.battlepass.BattlePassManager;
import de.goodzclan.gooDzSky.battlepass.PlayerBattlePassData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BattlePassCommand implements CommandExecutor {

    private final GooDzSky plugin;
    private final BattlePassManager battlePassManager;

    public BattlePassCommand(GooDzSky plugin, BattlePassManager battlePassManager) {
        this.plugin = plugin;
        this.battlePassManager = battlePassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl verwenden.");
            return true;
        }
        if (plugin.getPlayerBattlePassData(player) == null) {
            player.sendMessage("§cDeine Battle-Pass-Daten werden noch geladen. Bitte warte einen Moment und versuche es erneut.");
            return true; // Wir brechen den Befehl hier sicher ab.
        }

        openBattlePassGUI(player);
        return true;
    }

    public void openBattlePassGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, battlePassManager.getGuiTitle());
        PlayerBattlePassData data = plugin.getPlayerBattlePassData(player);

        // --- DEBUG-NACHRICHT ---
        plugin.getLogger().info("[DEBUG] Erstelle GUI für " + player.getName() + " | Aktuelles Level: " + data.getCurrentLevel());

        for (int i = 1; i <= 20; i++) {
            BattlePassLevel level = battlePassManager.getLevels().get(i);
            ItemStack displayItem;

            if (level == null) {
                gui.setItem(i - 1, new ItemStack(Material.BARRIER));
                continue;
            }

            // --- DEBUG-NACHRICHT ---
            boolean canClaim = data.canClaim(i);
            plugin.getLogger().info("[DEBUG] Prüfe Level " + i + ": Kann Spieler abholen? " + canClaim);

            if (data.hasClaimed(i)) {
                displayItem = createClaimedItem(level);
            } else if (canClaim) {
                displayItem = createUnclaimedItem(level);
            } else {
                displayItem = createLockedItem(level);
            }

            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(GooDzSky.BATTLEPASS_LEVEL_KEY, PersistentDataType.INTEGER, i);
                displayItem.setItemMeta(meta);
            }
            gui.setItem(i - 1, displayItem);
        }

        ItemStack levelUpButton = battlePassManager.getLevelUpButton();
        ItemMeta meta = levelUpButton.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.replaceAll(s -> s.replace("%cost%", String.valueOf(battlePassManager.getLevelUpCost())));
            meta.setLore(lore);
        }
        meta.getPersistentDataContainer().set(GooDzSky.BATTLEPASS_ACTION_KEY, PersistentDataType.STRING, "LEVEL_UP");
        levelUpButton.setItemMeta(meta);
        gui.setItem(49, levelUpButton);

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
    }

    private ItemStack createLockedItem(BattlePassLevel level) {
        ItemStack item = battlePassManager.getLockedLevelItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(meta.getDisplayName().replace("%level%", String.valueOf(level.getLevel())));
            item.setItemMeta(meta); // KORREKTUR: Metadaten zurückspeichern
        }
        return item;
    }

    private ItemStack createUnclaimedItem(BattlePassLevel level) {
        ItemStack item = new ItemStack(level.getDisplayMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + level.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Du hast diese Stufe erreicht!");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Klicke zum Abholen!");
            meta.setLore(lore);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createClaimedItem(BattlePassLevel level) {
        ItemStack item = battlePassManager.getClaimedLevelItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(meta.getDisplayName().replace("%level%", String.valueOf(level.getLevel())));
            item.setItemMeta(meta); // KORREKTUR: Metadaten zurückspeichern
        }
        return item;
    }
}