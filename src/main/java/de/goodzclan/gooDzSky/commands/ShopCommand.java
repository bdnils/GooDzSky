package de.goodzclan.gooDzSky.commands;

import de.goodzclan.gooDzSky.manager.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class ShopCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public ShopCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können den Shop benutzen.");
            return true;
        }
        Player p = (Player) sender;
        openSellShop(p);
        return true;
    }

    public void openSellShop(Player p) {
        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, shopManager.getSellShopTitle());

        // Alles mit Platzhalter füllen
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, shopManager.getPlaceholderItem());
        }

        // Sell-All Button
        inv.setItem(4, shopManager.getSellAllButton());
        // Switch-to-Buy Button
        inv.setItem(45, shopManager.getSwitchToBuyButton());

        // Verkaufs-Items platzieren
        int[] allowedSlots = getInnerSlots();
        int index = 0;
        for (Map.Entry<Material, Double> e : shopManager.getSellPrices().entrySet()) {
            if (index >= allowedSlots.length) break;
            inv.setItem(allowedSlots[index], createSellItem(e.getKey(), e.getValue()));
            index++;
        }

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    public void openBuyShop(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, shopManager.getBuyShopTitle());

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, shopManager.getPlaceholderItem());
        }

        // Switch-to-Sell Button
        inv.setItem(45, shopManager.getSwitchToSellButton());

        // Kauf-Items platzieren
        int[] allowedSlots = getInnerSlots();
        int index = 0;
        for (Map.Entry<Material, Double> e : shopManager.getBuyPrices().entrySet()) {
            if (index >= allowedSlots.length) break;
            inv.setItem(allowedSlots[index], createBuyItem(e.getKey(), e.getValue()));
            index++;
        }

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    private ItemStack createSellItem(Material mat, double price) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName("§a" + friendlyName(mat));
        meta.setLore(Arrays.asList(
                "§7Preis: §e" + price + " Coins/Stück",
                "§7Linksklick: §aAlles verkaufen",
                "§7Rechtsklick: §eNur eins verkaufen"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(meta);
        return is;
    }

    private ItemStack createBuyItem(Material mat, double price) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName("§a" + friendlyName(mat));
        meta.setLore(Arrays.asList(
                "§7Preis: §e" + price + " Coins/Stück",
                "§7Linksklick: §a1x kaufen",
                "§7Rechtsklick: §eStack kaufen"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(meta);
        return is;
    }

    private int[] getInnerSlots() {
        int[] slots = new int[35]; // 5 Reihen * 7 Spalten
        int idx = 0;
        for (int row = 1; row < 6; row++) {
            for (int col = 1; col < 8; col++) {
                if (idx < slots.length) {
                    slots[idx++] = row * 9 + col;
                }
            }
        }
        return slots;
    }

    private String friendlyName(Material m) {
        String[] parts = m.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}