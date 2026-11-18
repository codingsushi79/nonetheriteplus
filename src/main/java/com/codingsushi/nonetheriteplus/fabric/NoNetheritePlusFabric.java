package com.codingsushi.nonetheriteplus.fabric;

import com.codingsushi.nonetheriteplus.common.NetheriteEntries;
import com.codingsushi.nonetheriteplus.common.NetheriteEntry;
import com.codingsushi.nonetheriteplus.common.YamlConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class NoNetheritePlusFabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("nonetheriteplus");
    private static final String DATAPACK_ID = "nonetheriteplus_dynamic";

    private MinecraftServer server;
    private Path dataDir;
    private YamlConfig config;
    private YamlConfig statsConfig;
    private Path statsFile;

    private final Map<String, Integer> statistics = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> worldConfigs = new HashMap<>();
    private boolean debugMode = false;
    private int purgeIntervalTicks = 0;
    private int purgeTickCounter = 0;

    private final List<NetheriteEntry> entries = NetheriteEntries.all();

    @Override
    public void onInitialize() {
        StripUpgradeTemplateFunction.register();
        StripUpgradeTemplateFunction.setPredicate((id, context) -> {
            String worldName = VersionCompat.dimensionId(context.getLevel());
            boolean strip = getWorldConfigValue("disable_upgrade_templates", worldName, false);
            if (strip) {
                incrementStatistic("total_template_removals");
                if (debugMode && config.getBoolean("debug.log_loot_events", false)) {
                    LOGGER.info("Removed netherite upgrade template from loot in {}", worldName);
                }
            }
            return strip;
        });

        dataDir = FabricLoader.getInstance().getConfigDir().resolve("nonetheriteplus");
        loadConfig();
        initializeStatistics();
        loadWorldConfigurations();
        debugMode = config.getBoolean("debug.enabled", false);

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveStatistics());
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
            tableBuilder.apply(StripUpgradeTemplateFunction.INSTANCE));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommand(dispatcher));

        LOGGER.info("NoNetheritePlus (Fabric) initialized");
    }

    private void onServerStarted(MinecraftServer server) {
        this.server = server;
        applyRecipeBlocklist();
        resetPurgeInterval();
    }

    // ---------------------------------------------------------------- config

    private void loadConfig() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            LOGGER.warn("Could not create config directory", e);
        }
        Path configFile = dataDir.resolve("config.yml");
        if (!Files.exists(configFile)) {
            YamlConfig defaults = YamlConfig.loadFromResource("config.yml", getClass().getClassLoader());
            try {
                defaults.save(configFile);
            } catch (IOException e) {
                LOGGER.warn("Could not write default config", e);
            }
        }
        config = YamlConfig.loadFromFile(configFile);
    }

    private void reloadConfig() {
        config = YamlConfig.loadFromFile(dataDir.resolve("config.yml"));
    }

    private void saveConfig() {
        try {
            config.save(dataDir.resolve("config.yml"));
        } catch (IOException e) {
            LOGGER.warn("Could not save config", e);
        }
    }

    private void loadWorldConfigurations() {
        worldConfigs.clear();
        if (!config.contains("worlds")) {
            return;
        }
        for (String worldName : config.getKeys("worlds", false)) {
            Map<String, Object> worldConfig = config.flatten("worlds." + worldName);
            worldConfigs.put(worldName, worldConfig);
        }
        if (debugMode) {
            LOGGER.info("Loaded configurations for {} worlds", worldConfigs.size());
        }
    }

    // ------------------------------------------------------------ recipe blocking

    private boolean isDisabledNetheriteItem(NetheriteEntry entry, String worldName) {
        if (worldName != null && worldConfigs.containsKey(worldName)) {
            Object override = worldConfigs.get(worldName).get(entry.configKey());
            if (override instanceof Boolean) {
                return (Boolean) override;
            }
        }
        return config.getBoolean(entry.configKey(), entry.defaultDisabled());
    }

    /**
     * Blocks disabled netherite upgrades by writing a generated datapack that
     * overrides each disabled recipe's ingredients with an unobtainable item,
     * then triggers a resource reload. This mirrors Paper's recipe removal
     * without needing mixins into version-specific smithing internals; the
     * tradeoff (agreed with the user) is that this is global, not per-world,
     * and materials are simply never consumed rather than being refunded --
     * there's nothing to refund since the recipe never matches.
     */
    private void applyRecipeBlocklist() {
        if (server == null) {
            return;
        }
        try {
            Path packRoot = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(DATAPACK_ID);
            Path recipeDir = packRoot.resolve("data").resolve("minecraft").resolve("recipe");
            Files.createDirectories(recipeDir);

            try (Stream<Path> existing = Files.exists(recipeDir) ? Files.list(recipeDir) : Stream.empty()) {
                for (Path path : existing.toList()) {
                    Files.deleteIfExists(path);
                }
            }

            Files.writeString(packRoot.resolve("pack.mcmeta"),
                VersionCompat.packMcmeta("NoNetheritePlus dynamic overrides"),
                StandardCharsets.UTF_8);

            int blocked = 0;
            for (NetheriteEntry entry : entries) {
                if (!isDisabledNetheriteItem(entry, null)) {
                    continue;
                }
                String recipePath = entry.recipeId().substring(entry.recipeId().indexOf(':') + 1);
                String json = "{"
                    + "\"type\":\"minecraft:smithing_transform\","
                    + "\"template\":\"minecraft:barrier\","
                    + "\"base\":\"minecraft:barrier\","
                    + "\"addition\":\"minecraft:barrier\","
                    + "\"result\":{\"id\":\"" + entry.itemId() + "\",\"count\":1}"
                    + "}";
                Files.writeString(recipeDir.resolve(recipePath + ".json"), json, StandardCharsets.UTF_8);
                blocked++;
            }

            PackRepository packRepository = server.getPackRepository();
            packRepository.reload();
            Set<String> enabled = new LinkedHashSet<>(packRepository.getSelectedIds());
            enabled.add("file/" + DATAPACK_ID);
            server.reloadResources(enabled);

            if (debugMode) {
                LOGGER.info("Applied netherite recipe blocklist ({} recipe(s) disabled)", blocked);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to apply netherite recipe blocklist", e);
        }
    }

    // ------------------------------------------------------------------ purge

    private void resetPurgeInterval() {
        if (!config.getBoolean("periodic_purge.enabled", false)) {
            purgeIntervalTicks = 0;
            return;
        }
        long intervalSeconds = Math.max(1, config.getLong("periodic_purge.interval_seconds", 60));
        purgeIntervalTicks = (int) (intervalSeconds * 20L);
        purgeTickCounter = 0;
        if (debugMode) {
            LOGGER.info("Periodic netherite purge enabled (every {} seconds)", intervalSeconds);
        }
    }

    private void onServerTick(MinecraftServer server) {
        if (purgeIntervalTicks <= 0) {
            return;
        }
        purgeTickCounter++;
        if (purgeTickCounter < purgeIntervalTicks) {
            return;
        }
        purgeTickCounter = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            int removed = removeNetheriteFromPlayer(player);
            if (removed > 0) {
                incrementStatistic("total_purged_items", removed);
                if (debugMode) {
                    LOGGER.info("Removed {} netherite item(s) from {}", removed, player.getName().getString());
                }
            }
        }
    }

    private int removeNetheriteFromPlayer(ServerPlayer player) {
        // Index-based access covers main inventory, armor and offhand on every
        // supported version (the backing lists were split up in 1.21.5+).
        int removed = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (isNetheriteMaterial(item)) {
                removed += item.getCount();
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        return removed;
    }

    private boolean isNetheriteMaterial(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (NetheriteEntry entry : entries) {
            if (entry.itemId().equals(id)) {
                return true;
            }
        }
        return NetheriteEntries.PURGE_ONLY_ITEMS.contains(id);
    }

    // -------------------------------------------------------------- statistics

    private void initializeStatistics() {
        if (!config.getBoolean("statistics.enabled", true)) {
            return;
        }
        statsFile = dataDir.resolve("statistics.yml");
        statsConfig = YamlConfig.loadFromFile(statsFile);

        statistics.put("total_refunds", statsConfig.getInt("total_refunds", 0));
        statistics.put("total_blocked_crafts", statsConfig.getInt("total_blocked_crafts", 0));
        statistics.put("total_template_removals", statsConfig.getInt("total_template_removals", 0));
        statistics.put("total_purged_items", statsConfig.getInt("total_purged_items", 0));

        for (NetheriteEntry entry : entries) {
            String key = statKey(entry);
            statistics.put(key, statsConfig.getInt(key, 0));
        }
    }

    private void saveStatistics() {
        if (statsConfig == null || !config.getBoolean("statistics.enabled", true)) {
            return;
        }
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            statsConfig.set(entry.getKey(), entry.getValue());
        }
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            LOGGER.warn("Could not save statistics", e);
        }
    }

    private void incrementStatistic(String key) {
        incrementStatistic(key, 1);
    }

    private void incrementStatistic(String key, int amount) {
        if (!config.getBoolean("statistics.enabled", true)) {
            return;
        }
        if (key.equals("total_purged_items") && !config.getBoolean("statistics.track_purged_items", true)) {
            return;
        }
        statistics.merge(key, amount, Integer::sum);
        if (config.getBoolean("statistics.log_to_file", false)) {
            saveStatistics();
        }
    }

    private String statKey(NetheriteEntry entry) {
        return entry.itemId().substring(entry.itemId().indexOf(':') + 1) + "_blocked";
    }

    private boolean getWorldConfigValue(String key, String worldName, boolean def) {
        if (worldConfigs.containsKey(worldName)) {
            Object value = worldConfigs.get(worldName).get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return config.getBoolean(key, def);
    }

    // ----------------------------------------------------------------- command

    private void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nonetherite")
            .executes(ctx -> {
                sendInfo(ctx.getSource());
                return 1;
            })
            .then(Commands.literal("reload")
                .requires(VersionCompat.requiresOp())
                .executes(ctx -> {
                    saveStatistics();
                    reloadConfig();
                    loadWorldConfigurations();
                    debugMode = config.getBoolean("debug.enabled", false);
                    applyRecipeBlocklist();
                    resetPurgeInterval();
                    ctx.getSource().sendSuccess(() -> Component.literal("§aNoNetheritePlus configuration reloaded successfully!"), true);
                    return 1;
                }))
            .then(Commands.literal("stats")
                .requires(VersionCompat.requiresOp())
                .executes(ctx -> {
                    sendStats(ctx.getSource());
                    return 1;
                }))
            .then(Commands.literal("statistics")
                .requires(VersionCompat.requiresOp())
                .executes(ctx -> {
                    sendStats(ctx.getSource());
                    return 1;
                }))
            .then(Commands.literal("debug")
                .requires(VersionCompat.requiresOp())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("§eDebug mode is currently: §f" + (debugMode ? "ON" : "OFF")), false);
                    return 1;
                })
                .then(Commands.argument("toggle", StringArgumentType.word())
                    .executes(ctx -> {
                        String toggle = StringArgumentType.getString(ctx, "toggle").toLowerCase();
                        if (toggle.equals("on")) {
                            debugMode = true;
                            config.set("debug.enabled", true);
                            saveConfig();
                            ctx.getSource().sendSuccess(() -> Component.literal("§aDebug mode enabled"), true);
                        } else if (toggle.equals("off")) {
                            debugMode = false;
                            config.set("debug.enabled", false);
                            saveConfig();
                            ctx.getSource().sendSuccess(() -> Component.literal("§aDebug mode disabled"), true);
                        } else {
                            ctx.getSource().sendFailure(Component.literal("§cUse §f/nonetherite debug <on|off>"));
                        }
                        return 1;
                    })))
            .then(Commands.literal("preset")
                .requires(VersionCompat.requiresOp())
                .then(Commands.literal("save")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> presetSave(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("load")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> presetLoad(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("list")
                    .executes(ctx -> presetList(ctx.getSource())))
                .then(Commands.literal("delete")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> presetDelete(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))));
    }

    private void sendInfo(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6=== No Netherite+ ==="), false);
        source.sendSuccess(() -> Component.literal("§eVersion: §f" + FabricLoader.getInstance()
            .getModContainer("nonetheriteplus")
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown")), false);
        source.sendSuccess(() -> Component.literal("§eAuthor: §fSushiMC, SquareDealSam"), false);
        source.sendSuccess(() -> Component.literal("§7Commands: §freload§7, §fstats§7, §fdebug§7, §fpreset"), false);
    }

    private void sendStats(CommandSourceStack source) {
        if (!config.getBoolean("statistics.enabled", true)) {
            source.sendFailure(Component.literal("§cStatistics tracking is disabled."));
            return;
        }
        source.sendSuccess(() -> Component.literal("§6=== NoNetheritePlus Statistics ==="), false);
        source.sendSuccess(() -> Component.literal("§eTotal Blocked Crafts: §f" + statistics.getOrDefault("total_blocked_crafts", 0)), false);
        source.sendSuccess(() -> Component.literal("§eTemplates Removed: §f" + statistics.getOrDefault("total_template_removals", 0)), false);
        source.sendSuccess(() -> Component.literal("§eItems Purged: §f" + statistics.getOrDefault("total_purged_items", 0)), false);
        for (NetheriteEntry entry : entries) {
            int count = statistics.getOrDefault(statKey(entry), 0);
            if (count > 0) {
                source.sendSuccess(() -> Component.literal("§7" + entry.itemId() + ": §f" + count), false);
            }
        }
    }

    private int presetSave(CommandSourceStack source, String name) {
        Map<String, Object> presetData = new HashMap<>();
        Map<String, Object> flat = config.flatten("");
        for (Map.Entry<String, Object> entry : flat.entrySet()) {
            if (!entry.getKey().startsWith("presets")) {
                presetData.put(entry.getKey(), entry.getValue());
            }
        }
        config.set("presets." + name, presetData);
        saveConfig();
        source.sendSuccess(() -> Component.literal("§aPreset '" + name + "' saved successfully!"), true);
        return 1;
    }

    @SuppressWarnings("unchecked")
    private int presetLoad(CommandSourceStack source, String name) {
        if (!config.contains("presets." + name)) {
            source.sendFailure(Component.literal("§cPreset '" + name + "' does not exist."));
            return 0;
        }
        Object presetObj = config.get("presets." + name);
        if (presetObj instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) presetObj).entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
        }
        saveConfig();
        loadWorldConfigurations();
        debugMode = config.getBoolean("debug.enabled", false);
        applyRecipeBlocklist();
        source.sendSuccess(() -> Component.literal("§aPreset '" + name + "' loaded successfully!"), true);
        return 1;
    }

    private int presetList(CommandSourceStack source) {
        if (!config.contains("presets")) {
            source.sendSuccess(() -> Component.literal("§7No presets saved yet."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("§6=== Available Presets ==="), false);
        for (String presetName : config.getKeys("presets", false)) {
            source.sendSuccess(() -> Component.literal("§e- §f" + presetName), false);
        }
        return 1;
    }

    private int presetDelete(CommandSourceStack source, String name) {
        if (!config.contains("presets." + name)) {
            source.sendFailure(Component.literal("§cPreset '" + name + "' does not exist."));
            return 0;
        }
        config.set("presets." + name, null);
        saveConfig();
        source.sendSuccess(() -> Component.literal("§aPreset '" + name + "' deleted successfully!"), true);
        return 1;
    }
}
