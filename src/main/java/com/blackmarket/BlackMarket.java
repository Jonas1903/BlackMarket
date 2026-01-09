package com.blackmarket;

import com.blackmarket.commands.BlackMarketCommand;
import com.blackmarket.data.DataManager;
import com.blackmarket.data.RotationManager;
import com.blackmarket.listeners.GUIListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for BlackMarket
 */
public class BlackMarket extends JavaPlugin {
    private DataManager dataManager;
    private RotationManager rotationManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        dataManager = new DataManager(this);
        rotationManager = new RotationManager(this, dataManager);

        // Register commands
        BlackMarketCommand command = new BlackMarketCommand(this, dataManager, rotationManager);
        getCommand("blackmarket").setExecutor(command);

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this, dataManager), this);

        // Start rotation scheduler
        rotationManager.startRotationScheduler();

        getLogger().info("BlackMarket plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Stop rotation scheduler
        if (rotationManager != null) {
            rotationManager.stopRotationScheduler();
        }

        // Save data
        if (dataManager != null) {
            dataManager.saveData();
        }

        getLogger().info("BlackMarket plugin disabled!");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public RotationManager getRotationManager() {
        return rotationManager;
    }
}
