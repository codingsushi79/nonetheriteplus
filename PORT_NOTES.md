# NeoForge port notes

Reimplementation of NoNetheritePlus for NeoForge, built for Minecraft 26.3,
26.2 and 1.21.4 (`-PmcTarget=26.3|26.2|1.21`; `26` is an alias for 26.3).

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

## 26.x support (added in 1.6.1)

Minecraft 26.x is unobfuscated -- Mojang no longer publishes mappings
because the game jar already uses Mojang's names. Earlier notes here said
26.x was blocked on missing mappings; that was the wrong way round.

- NeoForge's event API is unchanged, but `LootModifier`'s constructor
  changed in both 26.2 (`conditions[], priority`) and 26.3
  (`Optional<Holder<LootItemCondition>>, priority`), so
  `StripUpgradeTemplateModifier` lives in `src/1.21`, `src/26.2` and
  `src/26.3`. `VersionCompat` in `src/1.21` / `src/26` covers the vanilla API
  differences (dimension id, op-permission check, `pack.mcmeta` format).
- Shared 26.x fixes (all versions): purge iterates inventory slots by index
  instead of the `items`/`armor`/`offhand` lists (split up in 1.21.5+);
  generated smithing recipes use plain-string ingredients (the
  `{"item": ...}` object form was dropped in 1.21.2); the generated datapack
  declares the running game's data-pack format instead of a hard-coded one.
- Bug fix: the global loot modifier list moved to
  `data/neoforge/loot_modifiers/global_loot_modifiers.json`. NeoForge only reads the list
  from the `neoforge` namespace, so in 1.6.0 the template-stripping modifier was
  never activated.
- The mod's `/nonetherite` version line now reads the real version from the
  mod container instead of printing a literal `${version}`.
- The netherite spear / horse armor recipe ids in `src/26` were checked
  against the real 26.3 server jar.

## Verified

`./gradlew build -PmcTarget=<26.3|26.2|1.21>` succeeds for all three
targets and produces `build/libs/NoNetheritePlus-<version>-neoforge-<26.3|26.2|1.21.x>.jar` (confirmed
contents: compiled classes, `neoforge.mods.toml` with version substituted,
bundled `config.yml`, GLM datapack JSON). Real compiler/Gradle
verification -- the two Forge-vs-NeoForge API differences above were found
by the compiler, not assumed.

**Not verified**: in-game behavior, for the same reason as the other
branches -- no way to launch a real Minecraft client/server here.
