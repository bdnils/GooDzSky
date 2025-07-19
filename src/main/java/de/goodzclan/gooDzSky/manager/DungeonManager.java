package de.goodzclan.gooDzSky.manager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import de.goodzclan.gooDzSky.GooDzSky;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;


public class DungeonManager {
    private final GooDzSky plugin;
    // Speichert, welche Mobs zu welchem Dungeon gehören
    private final Map<UUID, DungeonInstance> activeDungeonsByMob = new HashMap<>();
    // Speichert die laufenden Instanzen
    private final List<DungeonInstance> activeInstances = new ArrayList<>();
  //  private ModelEngineAPI modelEngineAPI;
    public enum Difficulty { LEICHT, MITTEL, SCHWER }

    public DungeonManager(GooDzSky plugin) {
        this.plugin = plugin;
    }

    public void startDungeon(List<Player> players, Difficulty difficulty, Location pasteLocation) {
        // Hier würdest du die Schematic einfügen (WorldEdit-API)
        // For now, let's just imagine it's pasted.
        // Location spawnPoint = pasteLocation.clone().add(5, 1, 5); // Beispiel-Spawnpunkt

        players.forEach(p -> p.sendMessage("§aDer Dungeon beginnt! Schwierigkeit: " + difficulty.name()));

        DungeonInstance instance = new DungeonInstance(players, difficulty);
        activeInstances.add(instance);

        spawnMobs(instance, pasteLocation);
    }

    private void spawnMobs(DungeonInstance instance, Location center) {
        Random random = new Random();
        List<UUID> mobUuids = new ArrayList<>();

        // Spawning-Logik basierend auf Schwierigkeit
        int vanillaMobs = 0;
        int customMobs = 0;
        String modelId = "dein_model_id"; // ID deines Models aus ModelEngine

        switch (instance.getDifficulty()) {
            case LEICHT:
                vanillaMobs = 10;
                customMobs = 2;
                modelId = "nocsy_otter_v2";
                break;
            case MITTEL:
                vanillaMobs = 15;
                customMobs = 5;
                modelId = "nocsy_otter_v2";
                break;
            case SCHWER:
                vanillaMobs = 20;
                customMobs = 10;
                modelId = "nocsy_otter_v2";
                break;
        }

        // Spawne Vanilla Mobs
        for (int i = 0; i < vanillaMobs; i++) {
            Location spawnLoc = center.clone().add(random.nextInt(10) - 5, 1, random.nextInt(10) - 5);
            Entity mob = center.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            mobUuids.add(mob.getUniqueId());
        }


        for (int i = 0; i < customMobs; i++) {

            Location spawnLocation = center.clone().add(random.nextInt(10) - 5, 1, random.nextInt(10) - 5);

            Entity baseEntity = spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.PIG);
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(baseEntity);
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
            modeledEntity.addModel(activeModel, true);
            modeledEntity.setBaseEntityVisible(false);


        }

        instance.setMobUuids(mobUuids);
        mobUuids.forEach(uuid -> activeDungeonsByMob.put(uuid, instance));
    }

    public void onMobDeath(Entity mob) {
        UUID mobId = mob.getUniqueId();
        if (activeDungeonsByMob.containsKey(mobId)) {
            DungeonInstance instance = activeDungeonsByMob.get(mobId);
            instance.getMobUuids().remove(mobId);
            activeDungeonsByMob.remove(mobId);

            if (instance.getMobUuids().isEmpty()) {
                endDungeon(instance);
            }
        }
    }

    private void endDungeon(DungeonInstance instance) {
        instance.getPlayers().forEach(player -> {
            player.sendMessage("§6Herzlichen Glückwunsch! Ihr habt den Dungeon abgeschlossen!");

            // 4% Chance auf einen Schlüssel
            if (Math.random() <= 0.04) { // Entspricht P(E) = 0.04
                ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
                ItemMeta meta = key.getItemMeta();
                meta.setDisplayName("§eDungeon Schlüssel");
                meta.setLore(Arrays.asList("§7Ein seltener Schlüssel aus einem Dungeon."));
                key.setItemMeta(meta);

                player.getInventory().addItem(key);
                player.sendMessage("§eDu hast einen seltenen Schlüssel erhalten!");
            }
        });
        activeInstances.remove(instance);
    }

    // Hilfsklasse für Dungeon-Instanzen
    public static class DungeonInstance {
        private final List<Player> players;
        private final Difficulty difficulty;
        private List<UUID> mobUuids;

        public DungeonInstance(List<Player> players, Difficulty difficulty) {
            this.players = players;
            this.difficulty = difficulty;
        }

        public List<Player> getPlayers() {
            return players;
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        public List<UUID> getMobUuids() {
            return mobUuids;
        }

        public void setMobUuids(List<UUID> mobUuids) {
            this.mobUuids = mobUuids;
        }
    }
}