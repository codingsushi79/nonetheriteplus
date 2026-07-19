# No Netherite +

Have you ever wanted to disable netherite armor on your server but all the plugins aren't really what you are looking for? Well look no more! NoNetherite+ allows you to configure what parts to disable!

**Compatibility:** Minecraft 26.x (Java 25) or 1.21.x (Java 21) on Paper/Spigot/Bukkit/Purpur/Folia. Also available for Fabric, Quilt, Forge, and NeoForge on 1.21.x (see below) — 26.x builds for those four aren't out yet; Mojang hasn't published mappings for 26.x, which the mod-loader toolchains need. Optional soft-depend on Paper: PlaceholderAPI.

<details>
<summary>Paper/Spigot/Bukkit/Purpur/Folia Configuration</summary>

The plugin creates `plugins/NoNetheritePlus/config.yml` (and `limits.yml`) on first startup. After editing, run `/nonetherite reload` to apply changes.

```yml
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
# When false, netherite armor can still receive smithing-table trims even if crafting that armor is disabled
disable_netherite_trims: false

# Player-facing messages
messages:
  blocked_craft: "&cNetherite crafting is disabled for this item. Your materials have been refunded."
  blocked_pickup: "&cYou cannot pick up this netherite item."
  stripped_on_join: "&cDisabled netherite items were removed from your inventory."
  blocked_equip: "&cEquipping this netherite armor is disabled."

# Stronger enforcement (all default off — existing servers stay unchanged)
enforcement:
  block_ancient_debris: false
  block_scrap_and_ingot_craft: false
  block_piglin_barter: false
  block_pickup: false
  strip_disabled_on_join: false
  block_equip_disabled: false
  convert_on_strip: false

# Soft possession limit (also mirrored in limits.yml)
limits:
  enabled: false
  max_netherite_items: 0

# Audit log of blocked actions
audit:
  enabled: false
  file: audit.log

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
| `refund_materials` | `true` | Refund the base item, ingot, and template when a blocked craft is attempted |
| `refund_message` | (see config) | Legacy refund message (prefer `messages.blocked_craft`). Supports `&` color codes |
| `disable_upgrade_templates` | `false` | Remove netherite upgrade smithing templates from generated loot |
| `disable_netherite_trims` | `false` | Block armor trim application on netherite armor |

### Messages

| Option | Description |
|--------|-------------|
| `messages.blocked_craft` | Shown when a smithing upgrade is blocked and refunded |
| `messages.blocked_pickup` | Shown when pickup is blocked |
| `messages.stripped_on_join` | Shown when disabled items are stripped on join |
| `messages.blocked_equip` | Shown when equipping disabled armor is blocked |

### Enforcement

All default `false` so existing servers stay unchanged.

| Option | Description |
|--------|-------------|
| `enforcement.block_ancient_debris` | Prevent ancient debris drops when mined |
| `enforcement.block_scrap_and_ingot_craft` | Block scrap smelting and ingot/block crafting |
| `enforcement.block_piglin_barter` | Strip netherite scrap/ingot/templates from piglin barters |
| `enforcement.block_pickup` | Cancel picking up restricted netherite items |
| `enforcement.strip_disabled_on_join` | Remove craft-disabled gear/tools on join |
| `enforcement.block_equip_disabled` | Cancel equipping craft-disabled netherite armor |
| `enforcement.convert_on_strip` | Convert stripped gear to diamond equivalents |

### Limits (`limits.yml`)

Soft possession limits are also mirrored under `limits:` in `config.yml`.

| Option | Default | Description |
|--------|---------|-------------|
| `limits.enabled` | `false` | Enable soft possession limits |
| `limits.max_netherite_items` | `0` | Max gear/tools in inventory (`0` = unlimited) |

### Audit

| Option | Default | Description |
|--------|---------|-------------|
| `audit.enabled` | `false` | Append blocked actions to an audit file |
| `audit.file` | `audit.log` | File name under the plugin data folder |

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

```yml
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

Presets are saved and loaded via commands (see below). The `presets:` section in config.yml is managed automatically — do not edit it by hand unless you know what you are doing.

</details>

<details>
<summary>Commands & Permissions (Paper/Spigot/Bukkit/Purpur/Folia)</summary>

On Fabric/Quilt/Forge/NeoForge, `/nonetherite`, `reload`, `stats`, `debug`, and `preset` work the same way, but `inspect`/`gui`/`audit` aren't available (not ported yet, see the loader-specific sections above), and there's no Bukkit-style permission node system — the admin subcommands just require operator (permission level 2) instead of the `nonetherite.*` nodes below.

### Commands

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
| `/nonetherite inspect [player]` | `nonetherite.inspect` | Summarize a player's netherite inventory |
| `/nonetherite gui` | `nonetherite.gui` | Open the config toggle GUI |
| `/nonetherite audit [lines]` | `nonetherite.audit` | Tail the audit log |

### Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `nonetherite.info` | all players | Access to `/nonetherite` |
| `nonetherite.bypass` | false | Skip all netherite restrictions |
| `nonetherite.reload` | op | Reload configuration |
| `nonetherite.stats` | op | View statistics |
| `nonetherite.debug` | op | Toggle debug mode |
| `nonetherite.preset` | op | Manage configuration presets |
| `nonetherite.inspect` | op | Inspect player inventories |
| `nonetherite.gui` | op | Open config GUI |
| `nonetherite.audit` | op | View audit log |
| `nonetherite.admin` | op | Parent for admin commands |

</details>

<details>
<summary>PlaceholderAPI</summary>

Optional soft-depend. Placeholders:

| Placeholder | Description |
|-------------|-------------|
| `%nonetherite_version%` | Plugin version |
| `%nonetherite_blocked_crafts%` | Total blocked crafts |
| `%nonetherite_disabled_<item>%` | Whether that item is craft-disabled (`helmet`, `netherite_sword`, etc.) |

</details>

<details>
<summary>Paper/Spigot/Bukkit/Purpur/Folia Installation</summary>

1. Place the matching `.jar` (26.x or 1.21.x) into your `plugins/` folder. Purpur and Folia both run the same jar as Paper.
2. Start the server.
3. When the server starts, a `NoNetheritePlus` folder will be created with `config.yml` and `limits.yml` inside. (See above for config.)
4. After making changes to the config file, run:

```
/nonetherite reload
```

to apply changes.

</details>

<details>
<summary>Fabric / Quilt</summary>

**1.21.x only for now** (see the compatibility note above for why 26.x isn't available yet).

Quilt runs the same jar as Fabric: Quilt Loader supports Fabric mods directly through Quilted Fabric API (QFAPI), and this mod only uses standard `fabric-api`, so there's no separate Quilt build. Install [Fabric API](https://modrinth.com/mod/fabric-api) (or QFAPI on Quilt) alongside it.

1. Install Fabric Loader (or Quilt Loader) for Minecraft 1.21.4.
2. Place the `fabric-1.21.x` jar and Fabric API/QFAPI into your `mods/` folder.
3. Start the server. A `config/nonetheriteplus/config.yml` file is created on first boot.
4. Run `/nonetherite reload` after editing the config to apply changes.

**Feature set on this build** is currently smaller than the Paper version: per-item `disable_netherite_*` toggles, `disable_upgrade_templates`, `periodic_purge`, world overrides (`worlds.*`), config presets, and a basic `statistics`/`debug` block are all supported. Not yet ported: `messages.*`, `enforcement.*`, `limits.*`, `audit.*`, `refund_materials`/`refund_message` (crafting is blocked by removing the recipe entirely, so there's nothing to refund), `disable_netherite_trims`, the GUI/inspect/audit commands, and PlaceholderAPI. Recipe blocking works by generating a datapack and reloading server resources rather than a live smithing-table hook, so toggling an item is instant via `/nonetherite reload`, same as Paper.

</details>

<details>
<summary>Forge</summary>

**1.21.x only for now.** Same feature set and config-file behavior as the Fabric/Quilt build above (see that section for the supported vs. not-yet-ported list).

1. Install Forge for Minecraft 1.21.4.
2. Place the `forge-1.21.x` jar into your `mods/` folder.
3. Start the server. A `config/nonetheriteplus/config.yml` file is created on first boot.
4. Run `/nonetherite reload` after editing the config to apply changes.

</details>

<details>
<summary>NeoForge</summary>

**1.21.x only for now.** Same feature set and config-file behavior as the Fabric/Quilt build above.

1. Install NeoForge for Minecraft 1.21.4.
2. Place the `neoforge-1.21.x` jar into your `mods/` folder.
3. Start the server. A `config/nonetheriteplus/config.yml` file is created on first boot.
4. Run `/nonetherite reload` after editing the config to apply changes.

</details>

Join the [Discord](https://discord.gg/2QU5WzN8hc)!
