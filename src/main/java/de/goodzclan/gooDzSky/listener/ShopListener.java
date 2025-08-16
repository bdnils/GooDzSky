package de.goodzclan.gooDzSky.listener;

import de.goodzclan.gooDzSky.GooDzSky;
import de.goodzclan.gooDzSky.commands.ShopCommand;
import de.goodzclan.gooDzSky.manager.ShopManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class ShopListener implements Listener {

    private final GooDzSky plugin;
    private final ShopManager shopManager;
    private final ShopCommand shopCommand;

    public ShopListener(GooDzSky plugin, ShopManager shopManager, ShopCommand shopCommand) {
        this.plugin = plugin;
        this.shopManager = shopManager;
        this.shopCommand = shopCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player)) return;
        Player player = (Player) ev.getWhoClicked();
        String viewTitle = ev.getView().getTitle();

        ItemStack clicked = ev.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Verkaufsmenü
        if (viewTitle.equals(shopManager.getSellShopTitle())) {
            ev.setCancelled(true);

            // Prüfen, ob ein GUI-Button geklickt wurde
            if (clicked.isSimilar(shopManager.getSwitchToBuyButton())) {
                shopCommand.openBuyShop(player);
                return;
            }
            if (clicked.isSimilar(shopManager.getSellAllButton())) {
                sellAll(player);
                return;
            }

            // Logik für den Verkauf von Items
            if (!shopManager.getSellPrices().containsKey(clicked.getType())) return;
            Material mat = clicked.getType();
            double pricePerUnit = shopManager.getSellPrices().get(mat);

            if (ev.isLeftClick()) {
                int totalCount = removeFromInventory(player, mat, -1);
                if (totalCount > 0) {
                    double total = totalCount * pricePerUnit;
                    plugin.depositToYourAPI(player, total);
                } else {
                    player.sendMessage("§cDu hast keine Items zum Verkaufen.");
                    plugin.playError(player);
                }
            } else if (ev.isRightClick()) {
                int removed = removeFromInventory(player, mat, 1);
                if (removed > 0) {
                    double total = removed * pricePerUnit;
                    plugin.depositToYourAPI(player, total);
                } else {
                    player.sendMessage("§cDu hast keine Items zum Verkaufen.");
                    plugin.playError(player);
                }
            }
        }

        // Kaufmenü
        else if (viewTitle.equals(shopManager.getBuyShopTitle())) {
            ev.setCancelled(true);

            if (clicked.isSimilar(shopManager.getSwitchToSellButton())) {
                shopCommand.openSellShop(player);
                return;
            }

            if (!shopManager.getBuyPrices().containsKey(clicked.getType())) return;
            Material mat = clicked.getType();
            double price = shopManager.getBuyPrices().get(mat);

            if (ev.isLeftClick()) {
                buyItem(player, mat, 1, price);
            } else if (ev.isRightClick()) {
                buyItem(player, mat, 64, price); // Feste Stack-Größe für Einfachheit
            }
        }
    }

    private void buyItem(Player player, Material mat, int amount, double pricePerUnit) {
        double totalPrice = pricePerUnit * amount;

        if (!plugin.withdrawFromYourAPI(player, totalPrice)) {
            // Die withdraw-Methode sendet bereits eine Nachricht
            return;
        }

        player.getInventory().addItem(new ItemStack(mat, amount));
        player.sendMessage("§aGekauft: §e" + amount + "x " + mat.name().replace("_", " ") + " §afür §e" + String.format("%.2f", totalPrice) + " Coins.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
    private int removeFromInventory(Player player, Material mat, int max) {
        PlayerInventory inv = player.getInventory();
        int toRemove = (max < 0) ? Integer.MAX_VALUE : max;
        int removed = 0;

        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != mat) continue;

            int amountInStack = stack.getAmount();
            if (toRemove >= amountInStack) {
                removed += amountInStack;
                toRemove -= amountInStack;
                contents[i] = null;
            } else {
                removed += toRemove;
                stack.setAmount(amountInStack - toRemove);
                toRemove = 0;
            }

            if (toRemove == 0) break;
        }

        inv.setContents(contents);
        player.updateInventory();
        return removed;
    }
    private void sellAll(Player player) {
        double totalCoins = 0;
        int itemsSold = 0;

        for (Map.Entry<Material, Double> e : shopManager.getSellPrices().entrySet()) {
            int removed = removeFromInventory(player, e.getKey(), -1);
            if (removed > 0) {
                itemsSold += removed;
                totalCoins += removed * e.getValue();
            }
        }

        if (itemsSold > 0) {
            plugin.depositToYourAPI(player, totalCoins);
        } else {
            player.sendMessage("§cDu hast keine Items im Inventar, die verkauft werden können.");
            plugin.playError(player);
        }
    }
}