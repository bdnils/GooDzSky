package de.goodzclan.gooDzSky.lootboxes;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootboxType {
    private final String name;
    private final ItemStack key;
    private final List<LootReward> rewards;
    private final Sound openSound;

    private final Location location;

    public LootboxType(String name, ItemStack key, List<LootReward> rewards, Sound openSound, Location location) {
        this.name = name;
        this.key = key;
        this.rewards = rewards;
        this.openSound = openSound;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }


    public String getName() { return name; }
    public ItemStack getKey() { return key; }
    public List<LootReward> getRewards() { return rewards; }
    public Sound getOpenSound() { return openSound; }
}
