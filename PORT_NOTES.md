# NeoForge port notes

Reimplementation of NoNetheritePlus for NeoForge, targeting Minecraft
1.21.x (26.x wired up in `build.gradle` but not currently buildable -- same
missing-Mojang-mappings issue as the Fabric/Forge branches).

Shares its design with the Forge branch almost entirely (same dynamic
recipe-block datapack, same Global Loot Modifier approach for template
stripping, same op-level permissions, same purge task) -- the code was
ported from Forge with `net.minecraftforge.*` -> `net.neoforged.*` package
renames and a couple of real API differences found via compilation, not
guessed:

- `LootModifier#doApply` is **2-arg** on NeoForge
  (`ObjectArrayList<ItemStack>, LootContext`), not the 3-arg
  (`LootTable, ObjectArrayList<ItemStack>, LootContext`) version Forge uses.
- NeoForge mods take the mod event bus as a **constructor parameter**
  (`IEventBus modEventBus`) instead of Forge's
  `FMLJavaModLoadingContext.get().getModEventBus()` static lookup.
- Mod metadata file is `META-INF/neoforge.mods.toml` (not `mods.toml`).
- `RegistryObject` -> `DeferredHolder`; `ForgeRegistries` -> `NeoForgeRegistries`;
  `MinecraftForge.EVENT_BUS` -> `NeoForge.EVENT_BUS`; tick events moved from
  `TickEvent.ServerTickEvent` (phase-based) to a dedicated
  `ServerTickEvent.Post` class.

## Build tooling

Uses NeoForge's modern `net.neoforged.moddev` Gradle plugin (2.0.142), which
has a much simpler DSL than ForgeGradle 7 -- no manual repository wiring
needed for the mod dependency itself:

```groovy
plugins {
    id 'net.neoforged.moddev' version '2.0.142'
}
neoForge {
    version = '21.4.157'
}
```

Confirmed via `settings.gradle` needing the NeoForged plugin repo
(`https://maven.neoforged.net/releases`) in `pluginManagement`. Snakeyaml is
bundled the same way as the Forge branch: Shadow plugin scoped to a
dedicated `shade` configuration, not the full runtime classpath.

## 26.x status

Same as Fabric/Forge: no 26.x Minecraft release has published mappings yet.
`-PmcTarget=26` picks NeoForge `26.1.0.25-beta` (confirmed to exist on
NeoForge's maven) but will fail at the same mapping-resolution step once
mappings become the blocker; wire-up is in place for when upstream catches
up.

## Verified

`./gradlew build -PmcTarget=1.21` succeeds and produces
`build/libs/NoNetheritePlus-<version>-neoforge-1.21.x.jar` (confirmed
contents: compiled classes, `neoforge.mods.toml` with version substituted,
bundled `config.yml`, GLM datapack JSON). Real compiler/Gradle
verification -- the two Forge-vs-NeoForge API differences above were found
by the compiler, not assumed.

**Not verified**: in-game behavior, for the same reason as the other
branches -- no way to launch a real Minecraft client/server here.
