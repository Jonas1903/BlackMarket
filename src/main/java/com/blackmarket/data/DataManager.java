package com.blackmarket.data;

import com.blackmarket.BlackMarket;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages data persistence for the black market
 */
public class DataManager {
    private final BlackMarket plugin;
    private final File dataFile;
    private YamlConfiguration data;

    private final Map<UUID, MarketItem> itemPool = new HashMap<>();
    private final List<UUID> activeItemIds = new ArrayList<>();
    private final Map<UUID, Set<UUID>> playerPurchases = new HashMap<>(); // Player UUID -> Set of purchased item IDs
    private long lastRotationTime;

    public DataManager(BlackMarket plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data file: " + e.getMessage());
                return;
            }
        }

        data = YamlConfiguration.loadConfiguration(dataFile);

        // Load item pool
        if (data.contains("item-pool")) {
            for (String key : data.getConfigurationSection("item-pool").getKeys(false)) {
                String path = "item-pool." + key;
                try {
                    UUID id = UUID.fromString(key);
                    byte[] itemBytes = data.getString(path + ".item").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
                    ItemStack itemStack = ItemStack.deserializeBytes(itemBytes);
                    
                    List<ItemStack> costItems = new ArrayList<>();
                    if (data.contains(path + ".costs")) {
                        List<String> costStrings = data.getStringList(path + ".costs");
                        for (String costStr : costStrings) {
                            byte[] costBytes = costStr.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
                            costItems.add(ItemStack.deserializeBytes(costBytes));
                        }
                    }
                    
                    int weight = data.getInt(path + ".weight", 1);
                    
                    MarketItem marketItem = new MarketItem(id, itemStack, costItems, weight);
                    itemPool.put(id, marketItem);
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not load market item " + key + ": " + e.getMessage());
                }
            }
        }

        // Load active items
        if (data.contains("active-items")) {
            List<String> activeIds = data.getStringList("active-items");
            for (String idStr : activeIds) {
                try {
                    activeItemIds.add(UUID.fromString(idStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in active items: " + idStr);
                }
            }
        }

        // Load player purchases
        if (data.contains("purchases")) {
            for (String playerUuidStr : data.getConfigurationSection("purchases").getKeys(false)) {
                try {
                    UUID playerUuid = UUID.fromString(playerUuidStr);
                    List<String> purchasedIds = data.getStringList("purchases." + playerUuidStr);
                    Set<UUID> purchasedSet = new HashSet<>();
                    
                    for (String itemIdStr : purchasedIds) {
                        try {
                            purchasedSet.add(UUID.fromString(itemIdStr));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid item UUID in purchases: " + itemIdStr);
                        }
                    }
                    
                    playerPurchases.put(playerUuid, purchasedSet);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player UUID in purchases: " + playerUuidStr);
                }
            }
        }

        // Load last rotation time
        lastRotationTime = data.getLong("last-rotation", System.currentTimeMillis());
    }

    public void saveData() {
        data = new YamlConfiguration();

        // Save item pool
        for (Map.Entry<UUID, MarketItem> entry : itemPool.entrySet()) {
            String path = "item-pool." + entry.getKey().toString();
            MarketItem item = entry.getValue();
            
            data.set(path + ".item", new String(item.getItemStack().serializeAsBytes(), java.nio.charset.StandardCharsets.ISO_8859_1));
            
            List<String> costStrings = new ArrayList<>();
            for (ItemStack cost : item.getCostItems()) {
                costStrings.add(new String(cost.serializeAsBytes(), java.nio.charset.StandardCharsets.ISO_8859_1));
            }
            data.set(path + ".costs", costStrings);
            data.set(path + ".weight", item.getWeight());
        }

        // Save active items
        List<String> activeIdStrings = new ArrayList<>();
        for (UUID id : activeItemIds) {
            activeIdStrings.add(id.toString());
        }
        data.set("active-items", activeIdStrings);

        // Save player purchases
        for (Map.Entry<UUID, Set<UUID>> entry : playerPurchases.entrySet()) {
            List<String> purchasedIds = new ArrayList<>();
            for (UUID itemId : entry.getValue()) {
                purchasedIds.add(itemId.toString());
            }
            data.set("purchases." + entry.getKey().toString(), purchasedIds);
        }

        // Save last rotation time
        data.set("last-rotation", lastRotationTime);

        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data file: " + e.getMessage());
        }
    }

    public void addItemToPool(MarketItem item) {
        itemPool.put(item.getId(), item);
        saveData();
    }

    public void removeItemFromPool(UUID id) {
        itemPool.remove(id);
        activeItemIds.remove(id);
        saveData();
    }

    public Map<UUID, MarketItem> getItemPool() {
        return new HashMap<>(itemPool);
    }

    public List<MarketItem> getActiveItems() {
        List<MarketItem> activeItems = new ArrayList<>();
        for (UUID id : activeItemIds) {
            MarketItem item = itemPool.get(id);
            if (item != null) {
                activeItems.add(item);
            }
        }
        return activeItems;
    }

    public void setActiveItems(List<UUID> itemIds) {
        activeItemIds.clear();
        activeItemIds.addAll(itemIds);
        saveData();
    }

    public boolean hasPlayerPurchased(UUID playerUuid, UUID itemId) {
        Set<UUID> purchases = playerPurchases.get(playerUuid);
        return purchases != null && purchases.contains(itemId);
    }

    public void recordPurchase(UUID playerUuid, UUID itemId) {
        playerPurchases.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(itemId);
        saveData();
    }

    public void clearPurchaseHistory() {
        playerPurchases.clear();
        saveData();
    }

    public long getLastRotationTime() {
        return lastRotationTime;
    }

    public void setLastRotationTime(long time) {
        this.lastRotationTime = time;
        saveData();
    }

    public MarketItem getMarketItem(UUID id) {
        return itemPool.get(id);
    }
}
