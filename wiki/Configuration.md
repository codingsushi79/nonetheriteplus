# Configuration Guide

Complete reference for all NoNetheritePlus configuration options.

## 📁 Configuration Files

NoNetheritePlus creates the following files in `plugins/NoNetheritePlus/`:

- **`config.yml`** - Main configuration file
- **`statistics.yml`** - Usage statistics (auto-generated)

## ⚙️ Main Configuration (config.yml)

### Netherite Armor Settings

Control which armor pieces can be crafted. When disabled, players cannot craft these items and materials are refunded.

```yaml
# Netherite Armor
disable_netherite_helmet: true      # Default: true - Prevents helmet crafting
disable_netherite_chestplate: true  # Default: true - Prevents chestplate crafting
disable_netherite_leggings: true    # Default: true - Prevents leggings crafting
disable_netherite_boots: true       # Default: true - Prevents boots crafting
```

### Netherite Tools Settings

Control tool crafting restrictions.

```yaml
# Netherite Tools
disable_netherite_sword: false      # Default: false - Allows sword crafting
disable_netherite_pickaxe: false   # Default: false - Allows pickaxe crafting
disable_netherite_axe: false       # Default: false - Allows axe crafting
disable_netherite_shovel: false    # Default: false - Allows shovel crafting
disable_netherite_hoe: false       # Default: false - Allows hoe crafting
```

### General Settings

Core plugin behavior configuration.

```yaml
# General Settings
refund_materials: true                    # Whether to refund materials when crafting is blocked
refund_message: "&cNetherite crafting is disabled for this item. Your materials have been refunded."
                                          # Message shown when refunding (supports color codes)
disable_upgrade_templates: false         # Whether to prevent netherite templates from spawning in loot
```

### Statistics & Analytics

Configure data collection and tracking.

```yaml
# Statistics & Analytics
statistics:
  enabled: true        # Master switch for statistics collection
  track_refunds: true  # Track material refunds given to players
  track_blocked_crafts: true  # Track attempts to craft restricted items
  track_template_removals: true  # Track removed upgrade templates
  reset_on_reload: false  # Reset statistics when config is reloaded
  log_to_file: false   # Save statistics immediately to disk (may impact performance)
```

### Debug Mode

Enhanced logging for troubleshooting.

```yaml
# Debug Mode
debug:
  enabled: false       # Enable detailed logging
  log_loot_events: false  # Log when templates are removed from loot
  log_craft_events: false # Log blocked crafting attempts
  log_config_changes: true # Log configuration updates
  verbose_messages: false  # Extra detailed console messages
```

### World-Specific Configurations

Override global settings for specific worlds.

```yaml
# World-Specific Configurations
worlds:
  world:              # World name (must match server world folder name)
    disable_netherite_helmet: false     # Override global setting
    disable_upgrade_templates: false    # Override global setting
    refund_materials: true             # Override global setting
  world_nether:       # Different settings for Nether world
    disable_upgrade_templates: true    # Disable templates in Nether
    disable_netherite_armor: true      # Quick way to disable all armor
  world_end:          # End world specific settings
    disable_upgrade_templates: true    # No templates in End
    refund_materials: false            # No refunds in End
# Note: Unspecified worlds use global config settings as defaults
```

### Configuration Presets

Auto-generated section for saving/loading configurations.

```yaml
# Configuration Presets (auto-generated)
presets: {}  # Managed via /nonetherite preset commands
```

## 📊 Statistics File (statistics.yml)

Auto-generated file tracking plugin usage (if enabled).

```yaml
# Example statistics.yml
total_refunds: 42
total_blocked_crafts: 156
total_template_removals: 23
netherite_helmet_blocked: 45
netherite_chestplate_blocked: 38
netherite_leggings_blocked: 32
netherite_boots_blocked: 41
# ... other item-specific counters
```

## 🔧 Configuration Management

### Reloading Configuration

After changing `config.yml`:

```bash
# In-game (requires permission)
reload

# Alternative: Restart server for guaranteed reload
```

### Configuration Validation

The plugin automatically validates configuration on load. Invalid settings fall back to defaults.

### Backup Configuration

```bash
# Manual backup
cp plugins/NoNetheritePlus/config.yml plugins/NoNetheritePlus/config.yml.backup

# Use presets for managed backups
/nonetherite preset save my_backup
```

## 🎯 Configuration Examples

### Complete Armor Ban (Default)
```yaml
disable_netherite_helmet: true
disable_netherite_chestplate: true
disable_netherite_leggings: true
disable_netherite_boots: true
refund_materials: true
```

### Hardcore Mode
```yaml
disable_upgrade_templates: true
disable_netherite_armor: true
disable_netherite_tools: true
refund_materials: false
statistics:
  enabled: true
```

### Creative Server
```yaml
disable_netherite_helmet: false
disable_netherite_chestplate: false
disable_netherite_leggings: false
disable_netherite_boots: false
disable_netherite_sword: false
disable_upgrade_templates: false
refund_materials: false
```

### Multi-World Setup
```yaml
# Global defaults
disable_upgrade_templates: false
refund_materials: true

worlds:
  world:  # Overworld - normal rules
    disable_netherite_helmet: false
  world_nether:  # Nether - restricted
    disable_upgrade_templates: true
  world_end:  # End - very restricted
    disable_upgrade_templates: true
    refund_materials: false
```

## ⚠️ Important Notes

### Configuration Priority
1. **World-specific settings** (highest priority)
2. **Global settings** (fallback)
3. **Plugin defaults** (failsafe)

### Performance Considerations
- **Debug mode**: Disable in production for better performance
- **Statistics logging**: `log_to_file: false` reduces disk I/O
- **World configs**: Only specify worlds that need different settings

### Common Issues

#### Changes Not Applying
```bash
# Always reload after config changes
/nonetherite reload
```

#### Invalid YAML Syntax
- Use a YAML validator (online tools available)
- Check indentation (spaces only, no tabs)
- Verify quotes around strings with special characters

#### World Names
- Must exactly match world folder names
- Case-sensitive
- Check `server.properties` for main world name

## 🔄 Advanced Configuration

### Conditional Logic
While not directly supported, you can achieve complex logic through world-specific settings and creative preset usage.

### Dynamic Configuration
For server networks, consider using symbolic links or shared configuration files.

### Monitoring Changes
Enable debug mode temporarily to monitor configuration effectiveness:

```yaml
debug:
  enabled: true
  log_config_changes: true
```

## 📞 Support

For configuration help:
- Check [troubleshooting guide](Troubleshooting.md)
- Review the [FAQ](FAQ.md)
- Create an issue on [GitHub](https://github.com/codingsushi79/nonetheriteplus/issues)

---

*Configuration syntax verified for version 1.4.0*
