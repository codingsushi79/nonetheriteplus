# NoNetheritePlus

A Minecraft plugin for Paper/Spigot 1.26+ that provides configurable netherite crafting restrictions with material refund system.

## Features

- **Configurable Restrictions**: Choose which netherite items to disable (armor, tools, or both)
- **Material Refund System**: Automatically refunds base materials when crafting is attempted
- **Template Control**: Option to completely disable netherite upgrade templates from spawning
- **Custom Messages**: Configurable refund notification messages
- **Per-Item Control**: Enable/disable each netherite item individually
- **Automatic Setup**: Creates config folder and file on first startup
- **Statistics & Analytics**: Track refunds, blocked crafts, and template removals
- **World-Specific Configurations**: Different settings per world for complex servers
- **Configuration Presets**: Save and load different config profiles
- **Debug Mode**: Enhanced logging for troubleshooting
- **Performance Optimizations**: Cached configurations and efficient event handling

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

## Configuration

The plugin creates a `config.yml` file in `plugins/NoNetheritePlus/` on first startup. Edit this file to customize behavior:

### Netherite Armor Settings
```yaml
# Set to true to disable crafting, false to allow normal crafting
disable_netherite_helmet: true      # Default: true
disable_netherite_chestplate: true  # Default: true
disable_netherite_leggings: true    # Default: true
disable_netherite_boots: true       # Default: true
```

### Netherite Tool Settings
```yaml
# Tools are enabled by default, set to true to disable
disable_netherite_sword: false      # Default: false
disable_netherite_pickaxe: false    # Default: false
disable_netherite_axe: false        # Default: false
disable_netherite_shovel: false     # Default: false
disable_netherite_hoe: false        # Default: false
```

### General Settings
```yaml
refund_materials: true  # Whether to refund materials when crafting is disabled
refund_message: "&cNetherite crafting is disabled for this item. Your materials have been refunded."
disable_upgrade_templates: false  # Whether to prevent netherite upgrade templates from spawning in loot
```

### Statistics & Analytics
```yaml
statistics:
  enabled: true        # Enable/disable statistics tracking
  track_refunds: true  # Track material refunds
  track_blocked_crafts: true  # Track blocked crafting attempts
  track_template_removals: true  # Track removed templates
  reset_on_reload: false  # Reset stats when config reloads
  log_to_file: false   # Save stats to file immediately
```

### Debug Mode
```yaml
debug:
  enabled: false       # Enable debug logging
  log_loot_events: false  # Log template removals
  log_craft_events: false # Log blocked crafts
  log_config_changes: true # Log config updates
  verbose_messages: false  # Extra detailed messages
```

### World-Specific Configurations
```yaml
worlds:
  world:              # World name
    disable_netherite_helmet: true
    disable_upgrade_templates: false
    refund_materials: true
  world_nether:       # Different settings per world
    disable_upgrade_templates: true
    disable_all_netherite: true
  world_end:          # Add other worlds as needed
    disable_upgrade_templates: true
# Settings fall back to global config for unspecified worlds
```

### Configuration Reload
After changing the config, restart your server or use a plugin manager to reload the plugin.

## Commands

- `/nonetherite` - Shows plugin version and information
- `/nonetherite reload` - Reloads configuration (requires `nonetherite.reload` permission)
- `/nonetherite stats` - Shows plugin statistics (requires `nonetherite.stats` permission)
- `/nonetherite debug <on|off>` - Toggle debug mode (requires `nonetherite.debug` permission)
- `/nonetherite preset save <name>` - Save current config as preset (requires `nonetherite.preset` permission)
- `/nonetherite preset load <name>` - Load a saved preset (requires `nonetherite.preset` permission)
- `/nonetherite preset list` - List available presets (requires `nonetherite.preset` permission)
- `/nonetherite preset delete <name>` - Delete a preset (requires `nonetherite.preset` permission)

## Permissions

- `nonetherite.info` - Access to basic plugin information (default: true)
- `nonetherite.reload` - Reload plugin configuration (default: op)
- `nonetherite.stats` - View statistics (default: op)
- `nonetherite.debug` - Toggle debug mode (default: op)
- `nonetherite.preset` - Manage configuration presets (default: op)

## Compatibility

- Minecraft 1.26+
- Paper/Spigot servers
- Requires Java 25
