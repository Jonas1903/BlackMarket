package com.blackmarket.gui;

import com.blackmarket.BlackMarket;
import com.blackmarket.data.DataManager;
import com.blackmarket.data.MarketItem;
import com.blackmarket.data.RotationManager;
import com.blackmarket.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Admin GUI for managing the black market
 */
public class AdminGUI {
    private final BlackMarket plugin;
    private final DataManager dataManager;
    private final RotationManager rotationManager;
    private static final String INVENTORY_TITLE = "Black Market Admin";

    public AdminGUI(BlackMarket plugin, DataManager dataManager, RotationManager rotationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.rotationManager = rotationManager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text(INVENTORY_TITLE));

        // Display all items in the pool
        Map<UUID, MarketItem> pool = dataManager.getItemPool();
        List<UUID> poolIds = new ArrayList<>(pool.keySet());

        int slot = 0;
        for (int i = 0; i < Math.min(45, poolIds.size()); i++) {
            UUID id = poolIds.get(i);
            MarketItem marketItem = pool.get(id);

            if (marketItem != null) {
                ItemStack displayItem = marketItem.getItemStack().clone();
                ItemMeta meta = displayItem.getItemMeta();

                if (meta != null) {
                    List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(ItemUtils.translateColorCodes("&eWeight: &6" + marketItem.getWeight())
                        .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.empty());
                    lore.add(ItemUtils.translateColorCodes("&6Cost Items:")
                        .decoration(TextDecoration.ITALIC, false));

                    if (marketItem.getCostItems().isEmpty()) {
                        lore.add(ItemUtils.translateColorCodes("&cNo cost set!")
                            .decoration(TextDecoration.ITALIC, false));
                    } else {
                        for (ItemStack cost : marketItem.getCostItems()) {
                            String costName = cost.getType().toString().toLowerCase().replace("_", " ");
                            if (cost.hasItemMeta() && cost.getItemMeta().hasDisplayName()) {
                                costName = cost.getItemMeta().displayName().toString();
                            }
                            lore.add(ItemUtils.translateColorCodes("&7- " + cost.getAmount() + "x " + costName)
                                .decoration(TextDecoration.ITALIC, false));
                        }
                    }

                    lore.add(Component.empty());
                    lore.add(ItemUtils.translateColorCodes("&eLeft-click: &7Edit costs")
                        .decoration(TextDecoration.ITALIC, false));
                    lore.add(ItemUtils.translateColorCodes("&eRight-click: &7Remove from pool")
                        .decoration(TextDecoration.ITALIC, false));

                    meta.lore(lore);
                    displayItem.setItemMeta(meta);
                }

                inventory.setItem(slot++, displayItem);
            }
        }

        // Add control buttons in the bottom row
        // Reload config button (slot 47)
        ItemStack reloadButton = ItemUtils.createItem(
            Material.COMMAND_BLOCK,
            "&eReload Config",
            "&7Click to reload config.yml",
            "&7(messages, rotation interval, etc.)"
        );
        inventory.setItem(47, reloadButton);

        // Force rotation button (slot 49)
        ItemStack rotateButton = ItemUtils.createItem(
            Material.EMERALD,
            "&aForce Rotation",
            "&7Click to rotate items now"
        );
        inventory.setItem(49, rotateButton);

        // Add item instruction (slot 53)
        ItemStack addInstruction = ItemUtils.createItem(
            Material.HOPPER,
            "&eAdd Items",
            "&7Drop items here to add them",
            "&7to the black market pool"
        );
        inventory.setItem(53, addInstruction);

        player.openInventory(inventory);
    }

    public static String getTitle() {
        return INVENTORY_TITLE;
    }
}
