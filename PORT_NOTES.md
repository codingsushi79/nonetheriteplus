# Fabric port notes

Reimplementation of NoNetheritePlus for Fabric, built for Minecraft 26.3,
26.2 and 1.21.4 (`-PmcTarget=26.3|26.2|1.21`; `26` is an alias for 26.3).

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

## 26.x support (added in 1.6.1)

Minecraft 26.x is unobfuscated -- Mojang no longer publishes mappings
because the game jar already uses Mojang's names. Earlier notes here said
26.x was blocked on missing mappings; that was the wrong way round.

- 1.21.4 is obfuscated, so it uses Loom's remapping plugin (`fabric-loom`)
  plus Mojang mappings. 26.x ships unobfuscated, so it uses the
  non-remapping `net.fabricmc.fabric-loom` plugin with plain `implementation`
  dependencies -- `build.gradle` picks the plugin from `-PmcTarget`.
- Version-specific code lives in `src/1.21/java` and `src/26/java`:
  `VersionCompat` (dimension id, op-permission check, `pack.mcmeta` format)
  and `StripUpgradeTemplateFunction` (`LootItemFunctionType` was replaced by
  plain `MapCodec`s in 26.x).
- Shared 26.x fixes (all versions): purge iterates inventory slots by index
  instead of the `items`/`armor`/`offhand` lists (split up in 1.21.5+);
  generated smithing recipes use plain-string ingredients (the
  `{"item": ...}` object form was dropped in 1.21.2); the generated datapack
  declares the running game's data-pack format instead of a hard-coded one.
- The netherite spear / horse armor recipe ids in `src/26` were checked
  against the real 26.3 server jar.

## Verified

`./gradlew build -PmcTarget=<26.3|26.2|1.21>` succeeds for all three
targets and produces `build/libs/NoNetheritePlus-<version>-fabric-<26.3|26.2|1.21.x>.jar` (confirmed
contents: compiled classes, `fabric.mod.json` with version substituted,
bundled `config.yml`, bundled `snakeyaml` jar). This is real compiler/Loom
verification, not a guess.

**Not verified**: in-game behavior. There's no way to launch an actual
Minecraft client/server in this environment. Before a real release, manually
test: smithing table recipe blocking (and that re-enabling via config +
`/nonetherite reload` actually un-blocks after the datapack regenerates),
loot template stripping in a couple of world types, the periodic purge task,
and every `/nonetherite` subcommand.
