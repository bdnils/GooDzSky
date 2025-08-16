package de.goodzclan.gooDzSky.commands;

import de.goodzclan.core.spigot.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigInteger;

public class PayCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl nutzen!");
            return true;
        }
        Player senderPlayer = (Player) sender;

        if (args.length != 2) {
            senderPlayer.sendMessage(ChatColor.RED + "Benutzung: /pay <Spieler> <Betrag>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            senderPlayer.sendMessage(ChatColor.RED + "Spieler nicht gefunden!");
            return true;
        }

        if (target.equals(senderPlayer)) {
            senderPlayer.sendMessage(ChatColor.RED + "Du kannst dir nicht selbst Geld schicken!");
            return true;
        }

        BigInteger amount;
        try {
            amount = new BigInteger(args[1]);
            if (amount.compareTo(BigInteger.ZERO) <= 0) {
                senderPlayer.sendMessage(ChatColor.RED + "Der Betrag muss positiv sein!");
                return true;
            }
        } catch (NumberFormatException e) {
            senderPlayer.sendMessage(ChatColor.RED + "Ungültiger Betrag!");
            return true;
        }

        PlayerData senderData = PlayerData.getPlayerDataByPlayer(senderPlayer);
        PlayerData targetData = PlayerData.getPlayerDataByPlayer(target);

        if (senderData.getCoins().compareTo(amount) < 0) {
            senderPlayer.sendMessage(ChatColor.RED + "Du hast nicht genug Münzen!");
            return true;
        }

        senderData.removeCoins(amount);
        targetData.addCoins(amount);

        senderPlayer.sendMessage(ChatColor.GREEN + "Du hast " + target.getName() + " " + amount + " Münzen geschickt!");
        target.sendMessage(ChatColor.YELLOW + senderPlayer.getName() + ChatColor.GREEN + " hat dir " + amount + " Münzen geschickt!");

        return true;
    }
}
