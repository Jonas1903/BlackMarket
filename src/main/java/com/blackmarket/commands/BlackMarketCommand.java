package com.blackmarket.commands;

import com.blackmarket.BlackMarket;
import com.blackmarket.data.DataManager;
import com.blackmarket.data.RotationManager;
import com.blackmarket.gui.AdminGUI;
import com.blackmarket.gui.PlayerShopGUI;
import com.blackmarket.utils.ItemUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for /blackmarket
 */
public class BlackMarketCommand implements CommandExecutor {
    private final BlackMarket plugin;
    private final DataManager dataManager;
    private final RotationManager rotationManager;

    public BlackMarketCommand(BlackMarket plugin, DataManager dataManager, RotationManager rotationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.rotationManager = rotationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            // Open admin GUI
            if (!player.hasPermission("blackmarket.admin") && !player.isOp()) {
                player.sendMessage(ItemUtils.translateColorCodes(
                    plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do this!")
                ));
                return true;
            }

            new AdminGUI(plugin, dataManager, rotationManager).open(player);
            player.sendMessage(ItemUtils.translateColorCodes(
                plugin.getConfig().getString("messages.admin-opened", "&eOpened admin menu")
            ));
            return true;
        }

        // Open player shop GUI
        new PlayerShopGUI(plugin, dataManager, rotationManager).open(player);
        return true;
    }
}
