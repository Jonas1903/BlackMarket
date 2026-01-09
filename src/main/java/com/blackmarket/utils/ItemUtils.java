package com.blackmarket.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for item operations
 */
public class ItemUtils {

    /**
     * Translate color codes in a string
     */
    public static Component translateColorCodes(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    /**
     * Create an ItemStack with display name and lore
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.displayName(translateColorCodes(name).decoration(TextDecoration.ITALIC, false));
            
            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(translateColorCodes(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Check if player has required items in inventory
     */
    public static boolean hasRequiredItems(org.bukkit.inventory.Inventory inventory, List<ItemStack> requiredItems) {
        ItemStack[] contents = inventory.getContents();
        
        for (ItemStack required : requiredItems) {
            int requiredAmount = required.getAmount();
            int foundAmount = 0;
            
            for (ItemStack item : contents) {
                if (item != null && item.isSimilar(required)) {
                    foundAmount += item.getAmount();
                }
            }
            
            if (foundAmount < requiredAmount) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Remove required items from player inventory
     */
    public static void removeRequiredItems(org.bukkit.inventory.Inventory inventory, List<ItemStack> requiredItems) {
        for (ItemStack required : requiredItems) {
            int amountToRemove = required.getAmount();
            
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                
                if (item != null && item.isSimilar(required)) {
                    if (item.getAmount() <= amountToRemove) {
                        amountToRemove -= item.getAmount();
                        inventory.setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - amountToRemove);
                        amountToRemove = 0;
                    }
                    
                    if (amountToRemove == 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Format time remaining in a readable format
     */
    public static String formatTimeRemaining(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
