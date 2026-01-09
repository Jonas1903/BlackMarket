package com.blackmarket.data;

import com.blackmarket.BlackMarket;
import org.bukkit.Bukkit;

import java.util.*;

/**
 * Manages the rotation of black market items
 */
public class RotationManager {
    private final BlackMarket plugin;
    private final DataManager dataManager;
    private int taskId = -1;

    public RotationManager(BlackMarket plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public void startRotationScheduler() {
        long rotationIntervalHours = plugin.getConfig().getLong("rotation-interval-hours", 24);
        long rotationIntervalTicks = rotationIntervalHours * 60 * 60 * 20; // Convert hours to ticks (20 ticks/second)

        // Check if rotation is needed on startup
        long timeSinceLastRotation = System.currentTimeMillis() - dataManager.getLastRotationTime();
        long rotationIntervalMillis = rotationIntervalHours * 60 * 60 * 1000;

        if (timeSinceLastRotation >= rotationIntervalMillis || dataManager.getActiveItems().isEmpty()) {
            // Perform rotation immediately if needed
            Bukkit.getScheduler().runTask(plugin, this::performRotation);
        }

        // Schedule periodic rotations
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, 
            this::performRotation, 
            rotationIntervalTicks, 
            rotationIntervalTicks
        );
    }

    public void stopRotationScheduler() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void performRotation() {
        Map<UUID, MarketItem> pool = dataManager.getItemPool();
        
        if (pool.isEmpty()) {
            plugin.getLogger().warning("Cannot rotate: item pool is empty");
            return;
        }

        // Use weighted random selection
        List<UUID> selectedIds = selectWeightedRandomItems(pool, Math.min(3, pool.size()));
        
        dataManager.setActiveItems(selectedIds);
        dataManager.clearPurchaseHistory();
        dataManager.setLastRotationTime(System.currentTimeMillis());
        
        plugin.getLogger().info("Black market items rotated. Selected " + selectedIds.size() + " items.");
    }

    /**
     * Select random items from the pool using weighted selection
     */
    private List<UUID> selectWeightedRandomItems(Map<UUID, MarketItem> pool, int count) {
        List<UUID> selected = new ArrayList<>();
        List<UUID> available = new ArrayList<>(pool.keySet());
        Random random = new Random();

        while (selected.size() < count && !available.isEmpty()) {
            // Calculate total weight of available items
            int totalWeight = 0;
            for (UUID id : available) {
                MarketItem item = pool.get(id);
                if (item != null) {
                    totalWeight += item.getWeight();
                }
            }

            if (totalWeight <= 0) {
                // If no items have weight, select randomly
                UUID randomId = available.remove(random.nextInt(available.size()));
                selected.add(randomId);
                continue;
            }

            // Select based on weight
            int randomWeight = random.nextInt(totalWeight);
            int currentWeight = 0;

            for (int i = 0; i < available.size(); i++) {
                UUID id = available.get(i);
                MarketItem item = pool.get(id);
                
                if (item != null) {
                    currentWeight += item.getWeight();
                    
                    if (randomWeight < currentWeight) {
                        selected.add(id);
                        available.remove(i);
                        break;
                    }
                }
            }
        }

        return selected;
    }

    public long getTimeUntilNextRotation() {
        long rotationIntervalHours = plugin.getConfig().getLong("rotation-interval-hours", 24);
        long rotationIntervalMillis = rotationIntervalHours * 60 * 60 * 1000;
        long timeSinceLastRotation = System.currentTimeMillis() - dataManager.getLastRotationTime();
        long timeRemaining = rotationIntervalMillis - timeSinceLastRotation;
        
        return Math.max(0, timeRemaining);
    }
}
