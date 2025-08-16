package de.goodzclan.gooDzSky.battlepass;

import java.util.HashSet;
import java.util.Set;

public class PlayerBattlePassData {

    private int currentLevel;
    private final Set<Integer> claimedLevels;

    /**
     * Standard-Konstruktor für neue Spieler, die noch keine Daten haben.
     * Setzt das Level auf 0.
     */
    public PlayerBattlePassData() {
        this.currentLevel = 0;
        this.claimedLevels = new HashSet<>();
    }

    /**
     * Konstruktor für Spieler, deren Daten aus der Datenbank geladen werden.
     * @param currentLevel Das geladene Level des Spielers.
     * @param claimedLevels Die Menge der bereits abgeholten Level.
     */
    public PlayerBattlePassData(int currentLevel, Set<Integer> claimedLevels) {
        this.currentLevel = currentLevel;
        this.claimedLevels = claimedLevels;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public Set<Integer> getClaimedLevels() {
        return claimedLevels;
    }

    public boolean hasClaimed(int level) {
        return claimedLevels.contains(level);
    }

    public boolean canClaim(int level) {
        return currentLevel >= level && !hasClaimed(level);
    }

    public void levelUp() {
        this.currentLevel++;
    }

    public void claimLevel(int level) {
        if (canClaim(level)) {
            this.claimedLevels.add(level);
        }
    }
}