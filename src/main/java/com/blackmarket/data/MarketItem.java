package com.blackmarket.data;

import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Represents an item in the black market pool
 */
public class MarketItem {
    private final UUID id;
    private final ItemStack itemStack;
    private final List<ItemStack> costItems;
    private int weight;

    public MarketItem(ItemStack itemStack, List<ItemStack> costItems, int weight) {
        this.id = UUID.randomUUID();
        this.itemStack = itemStack.clone();
        this.costItems = new ArrayList<>();
        for (ItemStack cost : costItems) {
            this.costItems.add(cost.clone());
        }
        this.weight = weight;
    }

    public MarketItem(UUID id, ItemStack itemStack, List<ItemStack> costItems, int weight) {
        this.id = id;
        this.itemStack = itemStack.clone();
        this.costItems = new ArrayList<>();
        for (ItemStack cost : costItems) {
            this.costItems.add(cost.clone());
        }
        this.weight = weight;
    }

    public UUID getId() {
        return id;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public List<ItemStack> getCostItems() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack cost : costItems) {
            copy.add(cost.clone());
        }
        return copy;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setCostItems(List<ItemStack> costItems) {
        this.costItems.clear();
        for (ItemStack cost : costItems) {
            this.costItems.add(cost.clone());
        }
    }
}
