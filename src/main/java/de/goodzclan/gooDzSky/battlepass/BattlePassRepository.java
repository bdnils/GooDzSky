package de.goodzclan.gooDzSky.battlepass;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import de.goodzclan.core.spigot.database.DatabaseHandler;
import de.goodzclan.gooDzSky.GooDzSky;
import net.soulmc.kronos.database.mongo.MongoDBDatabase;
import org.bson.Document;
import org.bson.conversions.Bson; // Wichtiger Import für den Filter
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BattlePassRepository {

    private final GooDzSky plugin;
    private final MongoDBDatabase database;
    private final MongoCollection<Document> collection;

    public BattlePassRepository(GooDzSky plugin, DatabaseHandler dbHandler) {
        this.plugin = plugin;
        this.database = dbHandler.getDatabase();
        this.collection = DatabaseHandler.getSkyBlockPlayerCollection();
    }

    /**
     * Lädt die Battle-Pass-Daten eines Spielers asynchron aus der Datenbank.
     * @param uuid Die UUID des Spielers.
     * @param callback Wird auf dem Hauptthread ausgeführt, sobald die Daten geladen sind.
     */
    public void loadPlayerDataAsync(UUID uuid, Consumer<PlayerBattlePassData> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Bson filter = Filters.eq("uuid", uuid.toString());
                Document foundDocument = database.find(collection, filter).first();

                PlayerBattlePassData data;
                if (foundDocument == null) {
                    // Spieler hat noch keine Daten, erstelle neue
                    data = new PlayerBattlePassData();
                } else {
                    // Lade existierende Daten aus dem Dokument
                    int currentLevel = foundDocument.getInteger("battlepass_level", 0);
                    List<Integer> claimedList = foundDocument.getList("battlepass_claimed", Integer.class, List.of());
                    Set<Integer> claimedSet = new HashSet<>(claimedList);
                    data = new PlayerBattlePassData(currentLevel, claimedSet);
                }

                // Gehe zurück zum Hauptthread, um die Daten sicher zu übergeben
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(data));

            } catch (Throwable t) { // Fängt alle Fehler ab, inkl. LinkageError
                plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der BattlePass-Daten für " + uuid, t);
                // Im Fehlerfall trotzdem leere Daten übergeben, um den Spieler nicht zu blockieren
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new PlayerBattlePassData()));
            }
        });
    }

    /**
     * Speichert die Battle-Pass-Daten eines Spielers asynchron in der Datenbank.
     * @param uuid Die UUID des Spielers.
     * @param data Das Datenobjekt des Spielers.
     */
    public void savePlayerDataAsync(UUID uuid, PlayerBattlePassData data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {

                Bson filter = Filters.eq("uuid", uuid.toString());

                Document battlePassFields = new Document()
                        .append("battlepass_level", data.getCurrentLevel())
                        .append("battlepass_claimed", data.getClaimedLevels());

                // $set sorgt dafür, dass nur diese Felder aktualisiert werden
                Document updateDocument = new Document("$set", battlePassFields);

                database.update(collection, filter, updateDocument, new UpdateOptions().upsert(true));
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE, "Fehler beim Speichern der BattlePass-Daten für " + uuid, t);
            }
        });
    }
}