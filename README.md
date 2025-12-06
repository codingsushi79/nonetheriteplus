# NoNetheriteExpanded

A Minecraft plugin for Paper/Spigot 1.21+ that provides configurable netherite crafting restrictions with material refund system.

## Features

- **Configurable Restrictions**: Choose which netherite items to disable (armor, tools, or both)
- **Material Refund System**: Automatically refunds base materials when crafting is attempted
- **Custom Messages**: Configurable refund notification messages
- **Per-Item Control**: Enable/disable each netherite item individually
- **Automatic Setup**: Creates config folder and file on first startup

## Building

### Prerequisites

- Java 21
- Gradle

### Build Commands

```bash
# Build the plugin JAR
gradle build

# Or use the gradle wrapper if available
./gradlew build
```

The compiled JAR will be located in `build/libs/NoNetheriteExpanded-1.2.0.jar`

## Installation

1. Copy the JAR file to your server's `plugins/` directory
2. Restart the server
3. The plugin will automatically create a `NoNetheriteExpanded` folder with `config.yml`

## Configuration

The plugin creates a `config.yml` file in `plugins/NoNetheriteExpanded/` on first startup. Edit this file to customize behavior:

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
```

### Configuration Reload
After changing the config, restart your server or use a plugin manager to reload the plugin.

## Commands

- `/nonetherite` - Shows plugin version and information
- `/nonetherite reload` - Reloads configuration (requires `nonetherite.reload` permission)

## Permissions

- `nonetherite.info` - Access to basic plugin information (default: true)
- `nonetherite.reload` - Reload plugin configuration (default: op)

## Compatibility

- Minecraft 1.21+
- Paper/Spigot servers
- Requires Java 21
