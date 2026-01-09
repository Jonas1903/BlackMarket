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

import java.util.ArrayList;
import java.util.List;

/**
 * Player shop GUI showing the current active items
 */
public class PlayerShopGUI {
    private final BlackMarket plugin;
    private final DataManager dataManager;
    private final RotationManager rotationManager;
    private static final String INVENTORY_TITLE = "Black Market";

    public PlayerShopGUI(BlackMarket plugin, DataManager dataManager, RotationManager rotationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.rotationManager = rotationManager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text(INVENTORY_TITLE));

        List<MarketItem> activeItems = dataManager.getActiveItems();

        // Place active items in slots 11, 13, 15
        int[] slots = {11, 13, 15};
        for (int i = 0; i < Math.min(activeItems.size(), 3); i++) {
            MarketItem marketItem = activeItems.get(i);
            ItemStack displayItem;

            boolean purchased = dataManager.hasPlayerPurchased(player.getUniqueId(), marketItem.getId());

            if (purchased) {
                // Show as sold out
                displayItem = ItemUtils.createItem(
                    Material.BARRIER,
                    plugin.getConfig().getString("messages.sold-out", "&cSOLD OUT"),
                    "&7You already purchased this item"
                );
            } else {
                displayItem = marketItem.getItemStack().clone();
                ItemMeta meta = displayItem.getItemMeta();

                if (meta != null) {
                    // Add cost information to lore
                    List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(ItemUtils.translateColorCodes("&6Cost:").decoration(TextDecoration.ITALIC, false));

                    for (ItemStack cost : marketItem.getCostItems()) {
                        String costName = cost.getType().toString().toLowerCase().replace("_", " ");
                        if (cost.hasItemMeta() && cost.getItemMeta().hasDisplayName()) {
                            costName = cost.getItemMeta().displayName().toString();
                        }
                        lore.add(ItemUtils.translateColorCodes("&7- " + cost.getAmount() + "x " + costName)
                            .decoration(TextDecoration.ITALIC, false));
                    }

                    meta.lore(lore);
                    displayItem.setItemMeta(meta);
                }
            }

            inventory.setItem(slots[i], displayItem);
        }

        // Add timer item in slot 22
        long timeRemaining = rotationManager.getTimeUntilNextRotation();
        String timeString = ItemUtils.formatTimeRemaining(timeRemaining);
        String timeMessage = plugin.getConfig().getString("messages.time-remaining", "&eTime until next rotation: &6{time}");
        timeMessage = timeMessage.replace("{time}", timeString);

        ItemStack timerItem = ItemUtils.createItem(
            Material.CLOCK,
            "&eRotation Timer",
            timeMessage,
            "&7Items rotate every " + plugin.getConfig().getLong("rotation-interval-hours", 24) + " hours"
        );
        inventory.setItem(22, timerItem);

        // Fill empty slots with glass panes
        ItemStack filler = ItemUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);
    }

    public static boolean isPlayerShopGUI(Inventory inventory) {
        return inventory.getHolder() == null && 
               Component.text(INVENTORY_TITLE).equals(inventory.getViewers().isEmpty() ? 
               Component.text(INVENTORY_TITLE) : 
               ((org.bukkit.inventory.InventoryView) ((Player) inventory.getViewers().get(0)).getOpenInventory()).title());
    }

    public static String getTitle() {
        return INVENTORY_TITLE;
    }
}
