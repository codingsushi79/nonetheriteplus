# Forge port notes

Reimplementation of NoNetheritePlus for Forge, built for Minecraft 26.3,
26.2 and 1.21.4 (`-PmcTarget=26.3|26.2|1.21`; `26` is an alias for 26.3).

## Design decisions (differ from the Paper plugin)

- **Recipe blocking**: same dynamic-datapack approach as the Fabric branch --
  disabled netherite recipes are blocked by writing a generated datapack
  (`<world>/datapacks/nonetheriteplus_dynamic/`) that overrides each disabled
  recipe's ingredients with `minecraft:barrier`, then calling
  `MinecraftServer#reloadResources`. Regenerated on server start and
  `/nonetherite reload`.
- **No "refund" step**: same reasoning as Fabric -- the recipe never
  matches, so nothing is ever consumed.
- **Permissions**: op-level (permission level 2) for admin subcommands.
- **Loot template stripping**: uses Forge's Global Loot Modifier system
  (`IGlobalLootModifier`/`LootModifier`, registered via
  `ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS` +
  `data/nonetheriteplus/loot_modifiers/*.json`), Forge's purpose-built,
  documented mechanism for exactly this ("modify generated loot across many
  tables without touching each one"). Functionally equivalent to the
  Fabric branch's custom `LootItemFunction` and to Paper's
  `LootGenerateEvent`.

## Build tooling notes (ForgeGradle 7 is a ground-up rewrite)

ForgeGradle 7.0.31's DSL is substantially different from FG5/6 (no more
`fg.deobf(...)`, no `reobf {}` block). Working pattern, confirmed via a real
build against the actual plugin classes (not guessed):

```groovy
minecraft {
    mappings channel: 'official', version: '1.21.4'
}
repositories {
    mavenCentral()
    maven { url 'https://maven.minecraftforge.net/' }   // Forge's own support libs
    maven { url 'https://libraries.minecraft.net/' }     // Mojang's com.mojang:* libs
    minecraft.mavenizer(repositories)                     // FG7's generated local repo
}
dependencies {
    implementation minecraft.dependency("net.minecraftforge:forge:1.21.4-54.1.17")
}
```

No `reobf` step is needed at all in FG7 -- Forge now builds directly against
official Mojang mappings end-to-end. Bundling snakeyaml uses the Shadow
plugin scoped to a dedicated `shade` configuration (not the whole
`runtimeClasspath`, which pulls in client-only LWJGL natives that don't
resolve for a server-only mod and shouldn't be bundled anyway).

## 26.x support (added in 1.6.1)

Minecraft 26.x is unobfuscated -- Mojang no longer publishes mappings
because the game jar already uses Mojang's names. Earlier notes here said
26.x was blocked on missing mappings; that was the wrong way round.

- ForgeGradle 7 builds 26.x directly (no mappings needed, the game ships
  unobfuscated).
- Forge 26.x moved to EventBus 7 (one static `BUS` per event, `BusGroup`
  instead of `IEventBus`), so event registration lives in a per-version
  `ForgeEventWiring` class in `src/1.21/java` / `src/26/java`. `VersionCompat`
  in the same folders covers the vanilla API differences (dimension id,
  op-permission check, `pack.mcmeta` format).
- Shared 26.x fixes (all versions): purge iterates inventory slots by index
  instead of the `items`/`armor`/`offhand` lists (split up in 1.21.5+);
  generated smithing recipes use plain-string ingredients (the
  `{"item": ...}` object form was dropped in 1.21.2); the generated datapack
  declares the running game's data-pack format instead of a hard-coded one.
- Bug fix: the global loot modifier list moved to
  `data/forge/loot_modifiers/global_loot_modifiers.json`. Forge only reads the list
  from the `forge` namespace, so in 1.6.0 the template-stripping modifier was
  never activated.
- The mod's `/nonetherite` version line now reads the real version from the
  mod container instead of printing a literal `${version}`.
- The netherite spear / horse armor recipe ids in `src/26` were checked
  against the real 26.3 server jar.

## Verified

`./gradlew build -PmcTarget=<26.3|26.2|1.21>` succeeds for all three
targets and produces `build/libs/NoNetheritePlus-<version>-forge-<26.3|26.2|1.21.x>.jar` (confirmed contents:
compiled classes, `META-INF/mods.toml` with version substituted, bundled
`config.yml`, bundled `snakeyaml`, GLM datapack JSON). Real compiler/Gradle
verification, not a guess -- several build-tooling issues (DSL differences,
missing repositories, shadowJar bundling client natives) were found and
fixed this way, not assumed correct.

**Not verified**: in-game behavior, for the same reason as the Fabric
branch -- no way to launch a real Minecraft client/server here. Manually
test before a real release: recipe blocking/re-enabling via
`/nonetherite reload`, loot template stripping, the periodic purge task,
and every `/nonetherite` subcommand.
