# NoNetheritePlus

A Minecraft plugin for Paper/Spigot that provides configurable netherite restrictions, enforcement, refunds, and admin tooling.

## Features

- **Configurable Restrictions**: Disable netherite armor/tools per item
- **Material Refunds**: Returns template, equipment, and ingot to inventory (drops on overflow)
- **Template & Trim Control**: Block upgrade templates in loot; optionally block armor trims
- **Stronger Enforcement** (defaults off): ancient debris, scrap/ingot craft, piglin barter, pickup, join-strip, equip block
- **Bypass Permission**: `nonetherite.bypass` skips all restrictions
- **Soft Limits**: Cap how many netherite gear/tools a player may hold (`limits.yml`)
- **Admin Tools**: inspect, config GUI, audit log, presets, stats, debug
- **PlaceholderAPI** (soft-depend): `%nonetherite_version%`, `%nonetherite_blocked_crafts%`, `%nonetherite_disabled_<item>%`
- **World-Specific Configurations** and **configuration presets**
- **Dual builds**: Minecraft 26.x (26.1–26.3, Java 25) and 1.21.x (Java 21)
- **Mod loaders**: Fabric/Quilt, Forge and NeoForge builds for 26.3, 26.2 and 1.21.4 (see the `fabric`, `forge` and `neoforge` branches)

## Building

### Prerequisites

- Java 25 (for `mcTarget=26`) or Java 21 (for `mcTarget=1.21`)
- Gradle wrapper included

### Build Commands

```bash
# Minecraft 26.x (compiled against 26.1.2 so one jar runs on 26.1–26.3)
./gradlew build -PmcTarget=26

# Compile-check against newer Paper APIs
./gradlew build -PmcTarget=26.2
./gradlew build -PmcTarget=26.3

# Minecraft 1.21.x
./gradlew build -PmcTarget=1.21
```

JARs land in `build/libs/` as `NoNetheritePlus-1.6.1-26.x.x.jar` / `NoNetheritePlus-1.6.1-1.21.x.jar`.

## Installation

1. Copy the matching JAR into `plugins/`
2. Restart the server
3. Edit `plugins/NoNetheritePlus/config.yml` (and optionally `limits.yml`)
4. Run `/nonetherite reload`

## Configuration highlights

### Enforcement (all default `false`)

```yaml
enforcement:
  block_ancient_debris: false
  block_scrap_and_ingot_craft: false
  block_piglin_barter: false
  block_pickup: false
  strip_disabled_on_join: false
  block_equip_disabled: false
  convert_on_strip: false   # convert stripped gear to diamond instead of deleting
```

### Messages

```yaml
messages:
  blocked_craft: "&cNetherite crafting is disabled for this item. Your materials have been refunded."
  blocked_pickup: "&cYou cannot pick up this netherite item."
  stripped_on_join: "&cDisabled netherite items were removed from your inventory."
  blocked_equip: "&cEquipping this netherite armor is disabled."
```

### Limits (`limits.yml`)

```yaml
limits:
  enabled: false
  max_netherite_items: 0   # 0 = unlimited
```

### Audit

```yaml
audit:
  enabled: false
  file: audit.log
```

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/nonetherite` | `nonetherite.info` | Plugin info |
| `/nonetherite reload` | `nonetherite.reload` | Reload config |
| `/nonetherite stats` | `nonetherite.stats` | View statistics |
| `/nonetherite debug <on\|off>` | `nonetherite.debug` | Toggle debug |
| `/nonetherite preset ...` | `nonetherite.preset` | Save/load/list/delete presets |
| `/nonetherite inspect [player]` | `nonetherite.inspect` | Inventory netherite summary |
| `/nonetherite gui` | `nonetherite.gui` | Chest config toggles |
| `/nonetherite audit [lines]` | `nonetherite.audit` | Tail audit log |

## Permissions

- `nonetherite.bypass` — skip all restrictions (default: false)
- `nonetherite.admin` — parent for reload/stats/debug/preset/inspect/gui/audit (default: op)
- Leaf perms: `info`, `reload`, `stats`, `debug`, `preset`, `inspect`, `gui`, `audit`

## PlaceholderAPI

| Placeholder | Value |
|-------------|-------|
| `%nonetherite_version%` | Plugin version |
| `%nonetherite_blocked_crafts%` | Total blocked crafts |
| `%nonetherite_disabled_helmet%` | `true`/`false` (also `disabled_netherite_helmet`, etc.) |

## Compatibility

- Minecraft **26.1–26.3** (Paper/Spigot/Bukkit/Purpur/Folia, Java 25) or **1.21.x** (Java 21)
- Fabric/Quilt, Forge, NeoForge: **26.3**, **26.2** and **1.21.4**
- Optional: PlaceholderAPI

## Releasing

Pushes to `main` run the **Build** workflow. Publishing a GitHub release builds every Paper and mod-loader JAR, uploads them to [Modrinth](https://modrinth.com/plugin/nonetherite+), attaches them to the GitHub release, and posts an `@everyone` announcement in Discord.

### One-time setup

1. Create a Modrinth PAT at [modrinth.com/settings/pats](https://modrinth.com/settings/pats) with the **Create versions** scope.
2. Create a Discord channel webhook (no bot required).
3. Add GitHub Actions secrets: `MODRINTH_TOKEN`, `DISCORD_WEBHOOK_URL`.

### Publish a new version

1. Bump `build.gradle` version (or rely on the release tag).
2. Create a GitHub release tagged like `v1.6.1`.
3. Publish — the Release workflow handles the rest.

### Join the [Discord!](https://discord.gg/2QU5WzN8hc)
