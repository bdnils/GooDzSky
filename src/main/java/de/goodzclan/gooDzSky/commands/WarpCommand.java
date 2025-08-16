package de.goodzclan.gooDzSky.commands;

import de.goodzclan.gooDzSky.GooDzSky;
import de.goodzclan.gooDzSky.manager.WarpManager;
import de.goodzclan.gooDzSky.manager.WarpTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /warp <name|add|remove|list>");
            return true;
        }

        // /warp add <name>
        if (args[0].equalsIgnoreCase("add")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cNur Spieler können diesen Befehl ausführen!");
                return true;
            }
            if (!sender.isOp()) {
                sender.sendMessage("§f[§6GooDzAttack§f] §cDu hast keine Berechtigung, diesen Befehl auszuführen!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§f[§6GooDzAttack§f] §cUsage: /warp add <name>");
                return true;
            }

            Player player = (Player) sender;
            String name = args[1];
            warpManager.addWarp(name, player.getLocation());
            sender.sendMessage("§f[§6GooDzAttack§f] §aWarp §e" + name + " §awurde hinzugefügt!");
            return true;
        }

        // /warp remove <name>
        if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.isOp()) {
                sender.sendMessage("§f[§6GooDzAttack§f] §cDu hast keine Berechtigung, diesen Befehl auszuführen!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§f[§6GooDzAttack§f] §cUsage: /warp remove <name>");
                return true;
            }

            String name = args[1];
            if (!warpManager.warpExists(name)) {
                sender.sendMessage("§f[§6GooDzAttack§f] §cWarp §e" + name + " §cexistiert nicht!");
                return true;
            }

            warpManager.removeWarp(name);
            sender.sendMessage("§f[§6GooDzAttack§f] §aWarp §e" + name + " §awurde entfernt!");
            return true;
        }

        // /warp list
        if (args[0].equalsIgnoreCase("list")) {
            Map<String, Location> warps = warpManager.getAllWarps();

            if (warps.isEmpty()) {
                sender.sendMessage("§f[§6GooDzAttack§f] §cEs gibt derzeit keine Warps.");
                return true;
            }

            String warpList = warps.keySet()
                    .stream()
                    .sorted()
                    .collect(Collectors.joining(", "));

            sender.sendMessage("§f[§6GooDzAttack§f] §aVerfügbare Warps: §e" + warpList);
            return true;
        }

        // Normaler /warp <name> Befehl
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können sich teleportieren!");
            return true;
        }

        Player player = (Player) sender;
        String warpName = args[0];
        Location warpLocation = warpManager.getWarp(warpName);

        if (warpLocation == null) {
            player.sendMessage("§f[§6GooDzAttack§f] §cWarp §e" + warpName + " §cexistiert nicht!");
            return true;
        }

        player.sendMessage("§f[§6GooDzAttack§f] §aTeleportation zu §e" + warpName + " §ain 5 Sekunden. Bitte bewege dich nicht!");

        // Starte die Teleportation mit Verzögerung
        new WarpTask(player, warpLocation, warpName, GooDzSky.getInstance()).start();
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();

            // OPs sehen Unterbefehle
            if (sender.isOp()) {
                options.add("add");
                options.add("remove");
                options.add("list");
            }

            // Alle sehen Warps
            options.addAll(warpManager.getAllWarps().keySet());

            return options.stream()
                    .filter(opt -> opt.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        else if (args.length == 2 && sender.isOp() && args[0].equalsIgnoreCase("remove")) {
            // Bei /warp remove -> Warps für OPs
            return warpManager.getAllWarps().keySet().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

}
