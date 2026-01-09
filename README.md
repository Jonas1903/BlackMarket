# BlackMarket Plugin

A Paper plugin for Minecraft 1.21.x that adds a black market trading system where items rotate daily and can be purchased with other items.

## Features

- **Item Pool Management**: Admins can add items to a pool with custom NBT data preserved
- **Weighted Random Selection**: Items are selected based on configurable weight values
- **Daily Rotation**: Market items rotate automatically every 24 hours (configurable)
- **Item-based Trading**: Players buy items using other items as currency, not virtual money
- **Purchase Limits**: Each player can only buy each item once per rotation
- **Full NBT Preservation**: All custom data, enchantments, names, and lore are preserved
- **Persistent Data**: All data is saved and persists through server restarts

## Commands

- `/blackmarket` or `/bm` - Opens the player shop GUI
- `/bm admin` - Opens the admin management GUI (requires `blackmarket.admin` permission or OP)

## Configuration

Edit `config.yml` to customize messages and rotation interval:

```yaml
rotation-interval-hours: 24
messages:
  purchase-success: "&aYou have successfully purchased this item!"
  already-purchased: "&cYou have already purchased this item!"
  not-enough-items: "&cYou don't have the required items!"
  no-permission: "&cYou don't have permission to do this!"
  rotation-forced: "&6The black market items have been rotated!"
```

## Usage

### For Players

1. Use `/bm` to open the black market shop
2. View the 3 currently available items
3. Click on an item to purchase it if you have the required items
4. Items you've already purchased will show as "SOLD OUT"
5. Check the clock item to see time until next rotation

### For Admins

1. Use `/bm admin` to open the admin panel
2. **Add items**: Shift-click items from your inventory into the admin GUI
3. **Edit costs**: Left-click an item to set what items are required to purchase it
4. **Adjust weight**: Use the weight buttons to change how likely an item is to be selected
5. **Remove items**: Right-click an item to remove it from the pool
6. **Force rotation**: Click the emerald button to immediately rotate items

### Cost Editor

1. Left-click an item in the admin GUI to open the cost editor
2. Place items in the lower slots to set the required cost items
3. Use the weight adjustment buttons to increase/decrease selection chance
4. Click the emerald to save or barrier to cancel

## Building

Requires Maven and Java 21:

```bash
mvn clean package
```

The compiled JAR will be in the `target/` directory.

## Technical Details

- **Data Storage**: Uses YAML format with Base64-encoded ItemStack serialization
- **Item Comparison**: Uses `ItemStack.isSimilar()` for accurate matching
- **Rotation Scheduler**: Runs on Bukkit scheduler with configurable intervals
- **GUI System**: Custom inventory-based GUIs with event handling

## File Structure

```
BlackMarket/
├── src/main/java/com/blackmarket/
│   ├── BlackMarket.java               # Main plugin class
│   ├── commands/
│   │   └── BlackMarketCommand.java    # Command handler
│   ├── gui/
│   │   ├── PlayerShopGUI.java         # Player shop interface
│   │   ├── AdminGUI.java              # Admin management interface
│   │   └── CostEditorGUI.java         # Cost/weight editor interface
│   ├── data/
│   │   ├── MarketItem.java            # Item model
│   │   ├── DataManager.java           # Data persistence
│   │   └── RotationManager.java       # Rotation logic
│   ├── listeners/
│   │   └── GUIListener.java           # GUI event handler
│   └── utils/
│       └── ItemUtils.java             # Utility functions
├── src/main/resources/
│   ├── plugin.yml                     # Plugin metadata
│   └── config.yml                     # Configuration
└── pom.xml                            # Maven build file
```

## Data Files

- `plugins/BlackMarket/config.yml` - Configuration
- `plugins/BlackMarket/data.yml` - Persistent data (item pool, active items, purchases)

## Requirements

- Paper 1.21.4+ (or compatible)
- Java 21+
- No other plugin dependencies

## License

This plugin was created for educational purposes.