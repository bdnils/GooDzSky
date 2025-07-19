package de.goodzclan.gooDzSky.listener;

import de.goodzclan.gooDzSky.manager.DungeonManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListener implements Listener {

    private final DungeonManager dungeonManager;

    public MobListener(DungeonManager dungeonManager) {
        this.dungeonManager = dungeonManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        // Nur fortfahren, wenn der Mob von einem Spieler getötet wurde
        if (killer == null) {
            return;
        }

        // Den Manager benachrichtigen, dass ein Mob gestorben ist
        dungeonManager.onMobDeath(entity);
    }
}