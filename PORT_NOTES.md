# Forge port notes

Reimplementation of NoNetheritePlus for Forge, targeting Minecraft 1.21.x
(26.x is configured in `build.gradle` but not currently buildable -- same
reason as the Fabric branch, see "26.x status" below).

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

## 26.x status

Same root cause as the Fabric branch: Mojang has not published official
mappings for any 26.x release (verified against piston-meta: 26.1, 26.1.1,
26.1.2, 26.2 all checked, none have them), and ForgeGradle's official
mapping channel needs that same data. `-PmcTarget=26` is wired up and will
work once mappings are published.

## Verified

`./gradlew build -PmcTarget=1.21` succeeds and produces
`build/libs/NoNetheritePlus-<version>-forge-1.21.x.jar` (confirmed contents:
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
