package com.blackmarket.gui;

import com.blackmarket.BlackMarket;
import com.blackmarket.data.DataManager;
import com.blackmarket.data.MarketItem;
import com.blackmarket.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GUI for editing the cost of a market item
 */
public class CostEditorGUI {
    private final BlackMarket plugin;
    private final DataManager dataManager;
    private final UUID itemId;
    private static final String INVENTORY_TITLE_PREFIX = "Edit Cost: ";

    public CostEditorGUI(BlackMarket plugin, DataManager dataManager, UUID itemId) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.itemId = itemId;
    }

    public void open(Player player) {
        MarketItem marketItem = dataManager.getMarketItem(itemId);
        if (marketItem == null) {
            player.sendMessage(ItemUtils.translateColorCodes("&cItem not found!"));
            return;
        }

        String title = INVENTORY_TITLE_PREFIX + itemId.toString().substring(0, 8);
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text(title));

        // Display current cost items in top row
        List<ItemStack> currentCosts = marketItem.getCostItems();
        for (int i = 0; i < Math.min(9, currentCosts.size()); i++) {
            inventory.setItem(i, currentCosts.get(i).clone());
        }

        // Add separator
        ItemStack separator = ItemUtils.createItem(Material.YELLOW_STAINED_GLASS_PANE, "&eCurrent Cost Items Above");
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, separator);
        }

        // Add instruction item
        ItemStack instruction = ItemUtils.createItem(
            Material.HOPPER,
            "&eSet Cost Items",
            "&7Place items in the slots below",
            "&7These will be the required items",
            "&7for purchasing this item"
        );
        inventory.setItem(18, instruction);

        // Add save button
        ItemStack saveButton = ItemUtils.createItem(
            Material.EMERALD,
            "&aSave Cost",
            "&7Click to save the cost items"
        );
        inventory.setItem(49, saveButton);

        // Add weight adjustment buttons
        ItemStack increaseWeight = ItemUtils.createItem(
            Material.LIME_DYE,
            "&aIncrease Weight",
            "&7Current: &6" + marketItem.getWeight(),
            "&7Click to increase by 1"
        );
        inventory.setItem(50, increaseWeight);

        ItemStack decreaseWeight = ItemUtils.createItem(
            Material.RED_DYE,
            "&cDecrease Weight",
            "&7Current: &6" + marketItem.getWeight(),
            "&7Click to decrease by 1"
        );
        inventory.setItem(51, decreaseWeight);

        // Add cancel button
        ItemStack cancelButton = ItemUtils.createItem(
            Material.BARRIER,
            "&cCancel",
            "&7Return without saving"
        );
        inventory.setItem(53, cancelButton);

        player.openInventory(inventory);
    }

    public static boolean isCostEditorGUI(String title) {
        return title != null && title.startsWith(INVENTORY_TITLE_PREFIX);
    }

    public static String getTitlePrefix() {
        return INVENTORY_TITLE_PREFIX;
    }

    public UUID getItemId() {
        return itemId;
    }
}
