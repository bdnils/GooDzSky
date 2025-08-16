package de.goodzclan.gooDzSky.lootboxes;

import org.bukkit.inventory.ItemStack;

public class LootReward {
    private final ItemStack item;
    private final double chance;

    public LootReward(ItemStack item, double chance) {
        this.item = item;
        this.chance = chance;
    }

    public ItemStack getItem() { return item; }
    public double getChance() { return chance; }
}

