package de.goodzclan.gooDzSky.battlepass;

import org.bukkit.Material;
import java.util.List;

public class BattlePassLevel {
    private final int level;
    private final String displayName;
    private final Material displayMaterial;
    private final List<String> rewardCommands;

    public BattlePassLevel(int level, String displayName, Material displayMaterial, List<String> rewardCommands) {
        this.level = level;
        this.displayName = displayName;
        this.displayMaterial = displayMaterial;
        this.rewardCommands = rewardCommands;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public Material getDisplayMaterial() { return displayMaterial; }
    public List<String> getRewardCommands() { return rewardCommands; }
}