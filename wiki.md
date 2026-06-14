# NoNetheritePlus

A Minecraft plugin for Paper/Spigot 1.26+ that provides configurable netherite crafting restrictions with material refund system.

## Features

- **Configurable Restrictions**: Choose which netherite items to disable (armor, tools, or both)
- **Material Refund System**: Automatically refunds base materials when crafting is attempted
- **Template Control**: Option to completely disable netherite upgrade templates from spawning
- **Periodic Purge**: Optionally remove all netherite items from online players on a timer
- **Custom Messages**: Configurable refund notification messages
- **Per-Item Control**: Enable/disable each netherite item individually
- **Automatic Setup**: Creates config folder and file on first startup
- **Statistics & Analytics**: Track refunds, blocked crafts, template removals, and purged items
- **World-Specific Configurations**: Different settings per world for complex servers
- **Configuration Presets**: Save and load different config profiles
- **Debug Mode**: Enhanced logging for troubleshooting
- **Performance Optimizations**: Cached configurations and efficient event handling

## Compatibility

- Minecraft 1.26+
- Paper/Spigot servers
- Requires Java 25

## Building

### Prerequisites

- Java 25
- Gradle

### Build Commands

```bash
# Build the plugin JAR
gradle build

# Or use the gradle wrapper if available
./gradlew build
```

The compiled JAR will be located in `build/libs/NoNetheritePlus-1.4.3.jar`

## Installation

1. Copy the JAR file to your server's `plugins/` directory
2. Restart the server
3. The plugin will automatically create a `NoNetheritePlus` folder with `config.yml`
4. After editing the config, run `/nonetherite reload` to apply changes

## Configuration

The plugin creates a `config.yml` file in `plugins/NoNetheritePlus/` on first startup. Edit this file to customize behavior.

### Full config example

```yaml
# NoNetheritePlus Configuration
# Set to true to disable crafting of netherite items and enable refund system
# Set to false to allow normal crafting

# Netherite Armor
disable_netherite_helmet: true
disable_netherite_chestplate: true
disable_netherite_leggings: true
disable_netherite_boots: true
disable_netherite_horse_armor: true

# Netherite Tools
disable_netherite_sword: false
disable_netherite_pickaxe: false
disable_netherite_axe: false
disable_netherite_shovel: false
disable_netherite_hoe: false
disable_netherite_spear: false

# General Settings
refund_materials: true
refund_message: "&cNetherite crafting is disabled for this item. Your materials have been refunded."
disable_upgrade_templates: false

# Periodic Netherite Purge
# Removes all netherite items from online players on a timer
periodic_purge:
  enabled: false
  interval_seconds: 60

# Statistics & Analytics
statistics:
  enabled: true
  track_refunds: true
  track_blocked_crafts: true
  track_template_removals: true
  track_purged_items: true
  reset_on_reload: false
  log_to_file: false

# Debug Mode
debug:
  enabled: false
  log_loot_events: false
  log_craft_events: false
  log_config_changes: true
  verbose_messages: false

# World-Specific Configurations
worlds:
  world:
  world_nether:
  world_end:
# If an item is not specified, the global config will be used.

# Configuration Presets (auto-generated)
presets: {}
```

### Netherite Armor

Set to `true` to block smithing that item and remove its recipe. Set to `false` to allow normal crafting.

| Option | Default | Description |
|--------|---------|-------------|
| `disable_netherite_helmet` | `true` | Block netherite helmet crafting |
| `disable_netherite_chestplate` | `true` | Block netherite chestplate crafting |
| `disable_netherite_leggings` | `true` | Block netherite leggings crafting |
| `disable_netherite_boots` | `true` | Block netherite boots crafting |
| `disable_netherite_horse_armor` | `true` | Block netherite horse armor crafting |

### Netherite Tools

Tools are allowed by default. Set to `true` to disable crafting for that item.

| Option | Default | Description |
|--------|---------|-------------|
| `disable_netherite_sword` | `false` | Block netherite sword crafting |
| `disable_netherite_pickaxe` | `false` | Block netherite pickaxe crafting |
| `disable_netherite_axe` | `false` | Block netherite axe crafting |
| `disable_netherite_shovel` | `false` | Block netherite shovel crafting |
| `disable_netherite_hoe` | `false` | Block netherite hoe crafting |
| `disable_netherite_spear` | `false` | Block netherite spear crafting |

### General Settings

| Option | Default | Description |
|--------|---------|-------------|
| `refund_materials` | `true` | Refund the base item and netherite ingot when a blocked craft is attempted |
| `refund_message` | (see config) | Message sent to the player when materials are refunded. Supports `&` color codes |
| `disable_upgrade_templates` | `false` | Remove netherite upgrade smithing templates from generated loot |

### Periodic Netherite Purge

Periodically strips all netherite items from every online player's inventory, armor, and offhand.

| Option | Default | Description |
|--------|---------|-------------|
| `periodic_purge.enabled` | `false` | Enable or disable the purge timer |
| `periodic_purge.interval_seconds` | `60` | How often to run the purge, in seconds (minimum 1) |

Removed items include armor, tools, ingots, scrap, blocks, and upgrade templates.

### Statistics & Analytics

| Option | Default | Description |
|--------|---------|-------------|
| `statistics.enabled` | `true` | Master toggle for statistics tracking |
| `statistics.track_refunds` | `true` | Count material refunds |
| `statistics.track_blocked_crafts` | `true` | Count blocked smithing attempts |
| `statistics.track_template_removals` | `true` | Count templates removed from loot |
| `statistics.track_purged_items` | `true` | Count items removed by periodic purge |
| `statistics.reset_on_reload` | `false` | Reset stats when the config is reloaded |
| `statistics.log_to_file` | `false` | Write stats to `statistics.yml` after every change |

View stats in-game with `/nonetherite stats`.

### Debug Mode

| Option | Default | Description |
|--------|---------|-------------|
| `debug.enabled` | `false` | Enable debug logging |
| `debug.log_loot_events` | `false` | Log template removals from loot |
| `debug.log_craft_events` | `false` | Log blocked crafts and stat updates |
| `debug.log_config_changes` | `true` | Log config reloads and updates |
| `debug.verbose_messages` | `false` | Extra detailed debug output |

Debug can also be toggled in-game with `/nonetherite debug <on|off>`.

### World-Specific Configurations

Override global settings per world. Any setting from the global config can be placed under a world name:

```yaml
worlds:
  world:
    disable_netherite_helmet: true
    disable_upgrade_templates: false
    refund_materials: true
  world_nether:
    disable_upgrade_templates: true
    disable_netherite_sword: true
  world_end:
    disable_upgrade_templates: true
```

Unspecified settings fall back to the global config.

### Configuration Presets

Presets are saved and loaded via commands. The `presets:` section in config.yml is managed automatically — do not edit it by hand unless you know what you are doing.

### Configuration Reload

After changing the config, restart your server or run `/nonetherite reload` to apply changes and restart the purge timer.

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/nonetherite` | `nonetherite.info` | Show plugin version and info |
| `/nonetherite reload` | `nonetherite.reload` | Reload config and restart purge timer |
| `/nonetherite stats` | `nonetherite.stats` | View plugin statistics |
| `/nonetherite debug <on\|off>` | `nonetherite.debug` | Toggle debug mode |
| `/nonetherite preset save <name>` | `nonetherite.preset` | Save current config as a preset |
| `/nonetherite preset load <name>` | `nonetherite.preset` | Load a saved preset |
| `/nonetherite preset list` | `nonetherite.preset` | List available presets |
| `/nonetherite preset delete <name>` | `nonetherite.preset` | Delete a preset |

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `nonetherite.info` | all players | Access to `/nonetherite` |
| `nonetherite.reload` | op | Reload configuration |
| `nonetherite.stats` | op | View statistics |
| `nonetherite.debug` | op | Toggle debug mode |
| `nonetherite.preset` | op | Manage configuration presets |
