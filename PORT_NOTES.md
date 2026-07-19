# Fabric port notes

Reimplementation of NoNetheritePlus for Fabric, targeting Minecraft 1.21.x
(26.x is configured in `build.gradle` but not currently buildable -- see
"26.x status" below).

## Design decisions (differ from the Paper plugin)

- **Recipe blocking**: instead of Paper's live `PrepareSmithingEvent`
  cancel+refund, disabled netherite recipes are blocked by writing a
  generated datapack (`<world>/datapacks/nonetheriteplus_dynamic/`) that
  overrides each disabled recipe's ingredients with `minecraft:barrier`
  (unobtainable in survival), then calling `MinecraftServer#reloadResources`.
  This is regenerated on server start and on `/nonetherite reload`. There's
  no mixin into version-specific smithing internals, which was a deliberate
  tradeoff agreed on to avoid shipping code that can't be runtime-verified
  in this environment.
- **No "refund" step**: since the recipe simply never matches, materials
  are never consumed in the first place -- nothing to refund. Arguably
  better UX than Paper's cancel-after-consumption flow.
- **Permissions**: op-level (permission level 2) for admin subcommands,
  matching plugin.yml's default `op` requirement. No fine-grained
  permission-plugin integration (no LuckPerms/Fabric Permissions API
  dependency) -- can be added later if needed.
- **Loot template stripping**: implemented via a custom `LootItemFunction`
  (`StripUpgradeTemplateFunction`) applied to every loot table through
  `LootTableEvents.MODIFY`, rather than `LootTableEvents.MODIFY_DROPS`
  (that event doesn't exist in the fabric-api version pinned to MC 1.21.4).
  The per-world disable check happens inside the function at drop-generation
  time via `LootContext`, so this stays behaviorally equivalent to Paper's
  per-world `LootGenerateEvent` handling despite being registered globally
  at table-load time.

## 26.x status

Confirmed against Mojang's version manifest (piston-meta) that **no 26.x
release (26.1, 26.1.1, 26.1.2, 26.2 all checked) has published
`client_mappings`/`server_mappings` yet**, unlike 1.21.4 which does. Fabric
Loom (and Quilt Loom / ForgeGradle / NeoGradle, which need the same data)
cannot deobfuscate the game jar without this, so building for 26.x currently
fails with "Failed to find official mojang mappings for 26.1.2" -- this is
an upstream gap, not a bug in this project. `-PmcTarget=26` is left wired up
in `build.gradle` and will work as soon as Mojang (or Yarn) publishes
mappings for a 26.x version.

## Verified

`./gradlew build -PmcTarget=1.21` succeeds and produces
`build/libs/NoNetheritePlus-<version>-fabric-1.21.x.jar` (confirmed
contents: compiled classes, `fabric.mod.json` with version substituted,
bundled `config.yml`, bundled `snakeyaml` jar). This is real compiler/Loom
verification, not a guess.

**Not verified**: in-game behavior. There's no way to launch an actual
Minecraft client/server in this environment. Before a real release, manually
test: smithing table recipe blocking (and that re-enabling via config +
`/nonetherite reload` actually un-blocks after the datapack regenerates),
loot template stripping in a couple of world types, the periodic purge task,
and every `/nonetherite` subcommand.
