package de.goodzclan.gooDzSky.commands;
import de.goodzclan.gooDzSky.manager.DungeonManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonCommand implements CommandExecutor {

    private final DungeonManager dungeonManager;

    public DungeonCommand(DungeonManager dungeonManager) {
        this.dungeonManager = dungeonManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player starter = (Player) sender;

        if (args.length < 2 || !args[0].equalsIgnoreCase("start")) {
            starter.sendMessage("§cBenutzung: /dungeon start <leicht|mittel|schwer>");
            return true;
        }

        DungeonManager.Difficulty difficulty;
        try {
            difficulty = DungeonManager.Difficulty.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            starter.sendMessage("§cUngültige Schwierigkeit. Wähle: leicht, mittel, schwer.");
            return true;
        }

        // Sammle bis zu 5 Spieler in einem Radius von 10 Blöcken
        List<Player> participants = starter.getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());

        if (!participants.contains(starter)) {
            participants.add(starter);
        }

        if (participants.size() > 5) {
            starter.sendMessage("§cEs können maximal 5 Spieler teilnehmen. Es sind zu viele in deiner Nähe.");
            return true;
        }

        // Starte den Dungeon
        dungeonManager.startDungeon(participants, difficulty, starter.getLocation());
        return true;
    }
}