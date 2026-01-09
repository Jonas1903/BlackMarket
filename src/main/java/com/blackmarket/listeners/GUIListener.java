package com.blackmarket.listeners;

import com.blackmarket.BlackMarket;
import com.blackmarket.data.DataManager;
import com.blackmarket.data.MarketItem;
import com.blackmarket.gui.AdminGUI;
import com.blackmarket.gui.CostEditorGUI;
import com.blackmarket.gui.PlayerShopGUI;
import com.blackmarket.utils.ItemUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Listener for GUI interactions
 */
public class GUIListener implements Listener {
    private final BlackMarket plugin;
    private final DataManager dataManager;
    private final Map<UUID, UUID> editingSessions = new HashMap<>(); // Player UUID -> Item ID being edited

    public GUIListener(BlackMarket plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // Handle Player Shop GUI
        if (title.equals(PlayerShopGUI.getTitle())) {
            handlePlayerShopClick(event, player, inventory);
            return;
        }

        // Handle Admin GUI
        if (title.equals(AdminGUI.getTitle())) {
            handleAdminGUIClick(event, player, inventory);
            return;
        }

        // Handle Cost Editor GUI
        if (CostEditorGUI.isCostEditorGUI(title)) {
            handleCostEditorClick(event, player, inventory, title);
            return;
        }
    }

    private void handlePlayerShopClick(InventoryClickEvent event, Player player, Inventory inventory) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Check if it's a filler or timer item
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE || clicked.getType() == Material.CLOCK) {
            return;
        }

        // Check if it's a barrier (sold out)
        if (clicked.getType() == Material.BARRIER) {
            player.sendMessage(ItemUtils.translateColorCodes(
                plugin.getConfig().getString("messages.already-purchased", "&cYou have already purchased this item!")
            ));
            return;
        }

        // Find the market item
        List<MarketItem> activeItems = dataManager.getActiveItems();
        int slot = event.getSlot();
        int[] itemSlots = {11, 13, 15};
        
        int itemIndex = -1;
        for (int i = 0; i < itemSlots.length; i++) {
            if (itemSlots[i] == slot) {
                itemIndex = i;
                break;
            }
        }

        if (itemIndex == -1 || itemIndex >= activeItems.size()) {
            return;
        }

        MarketItem marketItem = activeItems.get(itemIndex);

        // Check if already purchased
        if (dataManager.hasPlayerPurchased(player.getUniqueId(), marketItem.getId())) {
            player.sendMessage(ItemUtils.translateColorCodes(
                plugin.getConfig().getString("messages.already-purchased", "&cYou have already purchased this item!")
            ));
            return;
        }

        // Check if player has required items
        if (!ItemUtils.hasRequiredItems(player.getInventory(), marketItem.getCostItems())) {
            player.sendMessage(ItemUtils.translateColorCodes(
                plugin.getConfig().getString("messages.not-enough-items", "&cYou don't have the required items!")
            ));
            return;
        }

        // Remove cost items and give reward
        ItemUtils.removeRequiredItems(player.getInventory(), marketItem.getCostItems());
        player.getInventory().addItem(marketItem.getItemStack());

        // Record purchase
        dataManager.recordPurchase(player.getUniqueId(), marketItem.getId());

        player.sendMessage(ItemUtils.translateColorCodes(
            plugin.getConfig().getString("messages.purchase-success", "&aYou have successfully purchased this item!")
        ));

        // Refresh GUI
        player.closeInventory();
        Bukkit.getScheduler().runTask(plugin, () -> {
            new PlayerShopGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
        });
    }

    private void handleAdminGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Handle reload config button (slot 47)
        if (event.getSlot() == 47) {
            event.setCancelled(true);
            if (clicked != null && clicked.getType() == Material.COMMAND_BLOCK) {
                plugin.reloadConfig();
                player.sendMessage(ItemUtils.translateColorCodes(
                    plugin.getConfig().getString("messages.config-reloaded", "&aConfig reloaded successfully!")
                ));
            }
            return;
        }

        // Handle force rotation button (slot 49)
        if (event.getSlot() == 49) {
            event.setCancelled(true);
            
            if (clicked != null && clicked.getType() == Material.EMERALD) {
                plugin.getRotationManager().performRotation();
                player.sendMessage(ItemUtils.translateColorCodes(
                    plugin.getConfig().getString("messages.rotation-forced", "&6The black market items have been rotated!")
                ));
                player.closeInventory();
            }
            return;
        }

        // Handle add item slot (slot 53) - allow dropping items with cursor
        if (event.getSlot() == 53) {
            event.setCancelled(true);
            if (cursor != null && cursor.getType() != Material.AIR) {
                // Add item from cursor to pool
                MarketItem newItem = new MarketItem(cursor.clone(), new ArrayList<>(), 1);
                dataManager.addItemToPool(newItem);
                
                player.sendMessage(ItemUtils.translateColorCodes(
                    plugin.getConfig().getString("messages.item-added", "&aItem added to the black market pool!")
                ));
                
                // Clear cursor
                event.setCursor(null);
                
                // Refresh GUI
                Bukkit.getScheduler().runTask(plugin, () -> {
                    new AdminGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
                });
            }
            return;
        }

        // Handle clicking on items in the pool or empty slots for adding items
        if (event.getSlot() < 45) {
            event.setCancelled(true);

            // Check if clicking on empty slot with item on cursor - add to pool
            if ((clicked == null || clicked.getType() == Material.AIR) && cursor != null && cursor.getType() != Material.AIR) {
                // Add item from cursor to pool
                MarketItem newItem = new MarketItem(cursor.clone(), new ArrayList<>(), 1);
                dataManager.addItemToPool(newItem);
                
                player.sendMessage(ItemUtils.translateColorCodes(
                    plugin.getConfig().getString("messages.item-added", "&aItem added to the black market pool!")
                ));
                
                // Clear cursor
                event.setCursor(null);
                
                // Refresh GUI
                Bukkit.getScheduler().runTask(plugin, () -> {
                    new AdminGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
                });
                return;
            }

            // Handle clicking on existing items in the pool
            if (clicked != null && clicked.getType() != Material.AIR) {
                // Find the item in the pool
                List<UUID> poolIds = new ArrayList<>(dataManager.getItemPool().keySet());
                if (event.getSlot() < poolIds.size()) {
                    UUID itemId = poolIds.get(event.getSlot());
                    MarketItem marketItem = dataManager.getMarketItem(itemId);

                    if (marketItem != null) {
                        if (event.getClick() == ClickType.LEFT) {
                            // Open cost editor
                            editingSessions.put(player.getUniqueId(), itemId);
                            new CostEditorGUI(plugin, dataManager, itemId).open(player);
                        } else if (event.getClick() == ClickType.RIGHT) {
                            // Remove from pool
                            dataManager.removeItemFromPool(itemId);
                            player.sendMessage(ItemUtils.translateColorCodes("&cItem removed from pool!"));
                            
                            // Refresh GUI
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                new AdminGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
                            });
                        }
                    }
                }
            }
            return;
        }

        // Handle adding items to pool via shift-click or placing
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            if (event.getClickedInventory() != inventory) {
                // Player is shift-clicking from their inventory
                event.setCancelled(true);
                
                if (clicked != null && clicked.getType() != Material.AIR) {
                    // Add to pool
                    MarketItem newItem = new MarketItem(clicked.clone(), new ArrayList<>(), 1);
                    dataManager.addItemToPool(newItem);
                    
                    player.sendMessage(ItemUtils.translateColorCodes(
                        plugin.getConfig().getString("messages.item-added", "&aItem added to the black market pool!")
                    ));
                    
                    // Remove item from player inventory
                    event.getClickedInventory().setItem(event.getSlot(), null);
                    
                    // Refresh GUI
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        new AdminGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
                    });
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    private void handleCostEditorClick(InventoryClickEvent event, Player player, Inventory inventory, String title) {
        ItemStack clicked = event.getCurrentItem();

        // Extract item ID from title
        String idPart = title.substring(CostEditorGUI.getTitlePrefix().length());
        UUID itemId = editingSessions.get(player.getUniqueId());

        if (itemId == null) {
            event.setCancelled(true);
            return;
        }

        // Allow manipulating items in slots 19-48 (the working area)
        if (event.getSlot() >= 19 && event.getSlot() < 49) {
            // Allow normal inventory operations here
            return;
        }

        event.setCancelled(true);

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Handle save button
        if (event.getSlot() == 49 && clicked.getType() == Material.EMERALD) {
            saveCostItems(player, inventory, itemId);
            return;
        }

        // Handle weight adjustment
        if (event.getSlot() == 50 && clicked.getType() == Material.LIME_DYE) {
            adjustWeight(player, itemId, 1);
            return;
        }

        if (event.getSlot() == 51 && clicked.getType() == Material.RED_DYE) {
            adjustWeight(player, itemId, -1);
            return;
        }

        // Handle cancel button
        if (event.getSlot() == 53 && clicked.getType() == Material.BARRIER) {
            editingSessions.remove(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                new AdminGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
            });
            return;
        }
    }

    private void saveCostItems(Player player, Inventory inventory, UUID itemId) {
        MarketItem marketItem = dataManager.getMarketItem(itemId);
        if (marketItem == null) {
            player.sendMessage(ItemUtils.translateColorCodes("&cItem not found!"));
            return;
        }

        // Collect items from slots 19-48
        List<ItemStack> costItems = new ArrayList<>();
        for (int i = 19; i < 49; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                costItems.add(item.clone());
            }
        }

        marketItem.setCostItems(costItems);
        dataManager.saveData();

        player.sendMessage(ItemUtils.translateColorCodes("&aCost items saved!"));
        editingSessions.remove(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> {
            new AdminGUI(plugin, dataManager, plugin.getRotationManager()).open(player);
        });
    }

    private void adjustWeight(Player player, UUID itemId, int delta) {
        MarketItem marketItem = dataManager.getMarketItem(itemId);
        if (marketItem == null) {
            return;
        }

        int newWeight = Math.max(1, marketItem.getWeight() + delta);
        marketItem.setWeight(newWeight);
        dataManager.saveData();

        player.sendMessage(ItemUtils.translateColorCodes("&eWeight set to: &6" + newWeight));

        // Refresh GUI
        Bukkit.getScheduler().runTask(plugin, () -> {
            new CostEditorGUI(plugin, dataManager, itemId).open(player);
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        
        // Clean up editing sessions when closing cost editor
        if (CostEditorGUI.isCostEditorGUI(title)) {
            editingSessions.remove(player.getUniqueId());
        }
    }
}
