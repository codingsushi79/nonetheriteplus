package com.codingsushi.nonetheriteplus.command;

import com.codingsushi.nonetheriteplus.NetheriteMaterials;
import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.audit.AuditLog;
import com.codingsushi.nonetheriteplus.config.ConfigService;
import com.codingsushi.nonetheriteplus.gui.ConfigGui;
import com.codingsushi.nonetheriteplus.policy.RestrictionPolicy;
import com.codingsushi.nonetheriteplus.service.LimitsService;
import com.codingsushi.nonetheriteplus.service.PurgeService;
import com.codingsushi.nonetheriteplus.stats.StatisticsService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class NoNetheriteCommand implements CommandExecutor, TabCompleter {

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final RestrictionPolicy policy;
    private final StatisticsService stats;
    private final AuditLog audit;
    private final PurgeService purge;
    private final LimitsService limits;
    private final ConfigGui gui;

    public NoNetheriteCommand(
        NoNetheritePlus plugin,
        ConfigService config,
        RestrictionPolicy policy,
        StatisticsService stats,
        AuditLog audit,
        PurgeService purge,
        LimitsService limits,
        ConfigGui gui
    ) {
        this.plugin = plugin;
        this.config = config;
        this.policy = policy;
        this.stats = stats;
        this.audit = audit;
        this.purge = purge;
        this.limits = limits;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6=== No Netherite+ ===");
            sender.sendMessage("§eVersion: §f" + plugin.getPluginMeta().getVersion());
            sender.sendMessage("§eAuthor: §f" + String.join(", ", plugin.getPluginMeta().getAuthors()));
            sender.sendMessage("§eDescription: §f" + plugin.getPluginMeta().getDescription());
            sender.sendMessage("§7Commands: §freload§7, §fstats§7, §fdebug§7, §fpreset§7, §finspect§7, §fgui§7, §faudit");
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            case "stats", "statistics" -> handleStats(sender);
            case "debug" -> handleDebug(sender, args);
            case "preset" -> handlePreset(sender, args);
            case "inspect" -> handleInspect(sender, args);
            case "gui" -> handleGui(sender);
            case "audit" -> handleAudit(sender, args);
            default -> {
                sender.sendMessage("§cUnknown subcommand. Use §f/nonetherite §cfor help.");
                yield true;
            }
        };
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("nonetherite.reload")) {
            sender.sendMessage("§cYou don't have permission to reload the configuration.");
            return true;
        }
        plugin.applyRuntimeReload(true);
        sender.sendMessage("§aNoNetheritePlus configuration reloaded successfully!");
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!sender.hasPermission("nonetherite.stats")) {
            sender.sendMessage("§cYou don't have permission to view statistics.");
            return true;
        }
        if (!config.getBoolean("statistics.enabled", true)) {
            sender.sendMessage("§cStatistics tracking is disabled.");
            return true;
        }

        sender.sendMessage("§6=== NoNetheritePlus Statistics ===");
        sender.sendMessage("§eTotal Refunds: §f" + stats.get("total_refunds"));
        sender.sendMessage("§eTotal Blocked Crafts: §f" + stats.get("total_blocked_crafts"));
        sender.sendMessage("§eTemplates Removed: §f" + stats.get("total_template_removals"));
        sender.sendMessage("§eItems Purged: §f" + stats.get("total_purged_items"));
        sender.sendMessage("§7--- Item Breakdown ---");
        for (Material material : NetheriteMaterials.gearAndTools()) {
            String key = material.name().toLowerCase(Locale.ROOT) + "_blocked";
            int count = stats.get(key);
            if (count > 0) {
                sender.sendMessage("§7" + material.name().replace("NETHERITE_", "") + ": §f" + count);
            }
        }
        return true;
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nonetherite.debug")) {
            sender.sendMessage("§cYou don't have permission to toggle debug mode.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§eDebug mode is currently: §f" + (config.isDebugMode() ? "ON" : "OFF"));
            sender.sendMessage("§7Use §f/nonetherite debug <on|off> §7to toggle");
            return true;
        }

        String toggle = args[1].toLowerCase(Locale.ROOT);
        if (toggle.equals("on")) {
            config.setDebugMode(true);
            sender.sendMessage("§aDebug mode enabled");
            plugin.getLogger().info("Debug mode enabled by " + sender.getName());
        } else if (toggle.equals("off")) {
            config.setDebugMode(false);
            sender.sendMessage("§aDebug mode disabled");
            plugin.getLogger().info("Debug mode disabled by " + sender.getName());
        } else {
            sender.sendMessage("§cUse §f/nonetherite debug <on|off>");
        }
        return true;
    }

    private boolean handlePreset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nonetherite.preset")) {
            sender.sendMessage("§cYou don't have permission to manage presets.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§7Usage: §f/nonetherite preset <save|load|list|delete> [name]");
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        return switch (action) {
            case "save" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: §f/nonetherite preset save <name>");
                    yield true;
                }
                config.savePreset(args[2]);
                sender.sendMessage("§aPreset '" + args[2] + "' saved successfully!");
                yield true;
            }
            case "load" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: §f/nonetherite preset load <name>");
                    yield true;
                }
                if (!config.loadPreset(args[2])) {
                    sender.sendMessage("§cPreset '" + args[2] + "' does not exist.");
                    yield true;
                }
                plugin.applyRuntimeReload(false);
                sender.sendMessage("§aPreset '" + args[2] + "' loaded successfully!");
                yield true;
            }
            case "list" -> {
                if (config.presetNames().isEmpty()) {
                    sender.sendMessage("§7No presets saved yet.");
                } else {
                    sender.sendMessage("§6=== Available Presets ===");
                    for (String name : config.presetNames()) {
                        sender.sendMessage("§e- §f" + name);
                    }
                }
                yield true;
            }
            case "delete" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: §f/nonetherite preset delete <name>");
                    yield true;
                }
                if (!config.deletePreset(args[2])) {
                    sender.sendMessage("§cPreset '" + args[2] + "' does not exist.");
                    yield true;
                }
                sender.sendMessage("§aPreset '" + args[2] + "' deleted successfully!");
                yield true;
            }
            default -> {
                sender.sendMessage("§cUnknown preset action. Use save, load, list, or delete.");
                yield true;
            }
        };
    }

    private boolean handleInspect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nonetherite.inspect")) {
            sender.sendMessage("§cYou don't have permission to inspect players.");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: §f" + args[1]);
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("§cUsage: §f/nonetherite inspect <player>");
            return true;
        }

        Map<Material, Integer> counts = purge.countByMaterial(target);
        sender.sendMessage("§6=== Inspect: " + target.getName() + " ===");
        sender.sendMessage("§eBypass: §f" + policy.hasBypass(target));
        sender.sendMessage("§eNetherite gear count: §f" + purge.countNetheriteGear(target)
            + (limits.isEnabled() ? " §7/ §f" + limits.maxNetheriteItems() : ""));
        if (counts.isEmpty()) {
            sender.sendMessage("§7No netherite items in inventory.");
        } else {
            for (Map.Entry<Material, Integer> entry : counts.entrySet()) {
                boolean disabled = policy.isDisabledNetheriteItem(entry.getKey(), target.getWorld());
                sender.sendMessage("§7" + entry.getKey().name().replace("NETHERITE_", "")
                    + ": §f" + entry.getValue() + (disabled ? " §c(disabled)" : ""));
            }
        }
        return true;
    }

    private boolean handleGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can open the config GUI.");
            return true;
        }
        if (!sender.hasPermission("nonetherite.gui")) {
            sender.sendMessage("§cYou don't have permission to open the config GUI.");
            return true;
        }
        gui.open(player);
        return true;
    }

    private boolean handleAudit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nonetherite.audit")) {
            sender.sendMessage("§cYou don't have permission to view the audit log.");
            return true;
        }
        int lines = 20;
        if (args.length >= 2) {
            try {
                lines = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUsage: §f/nonetherite audit [lines]");
                return true;
            }
        }
        List<String> tail = audit.tail(lines);
        sender.sendMessage("§6=== Audit Log (last " + tail.size() + ") ===");
        if (tail.isEmpty()) {
            sender.sendMessage("§7No audit entries yet. Enable audit.enabled in config.yml.");
        } else {
            for (String line : tail) {
                sender.sendMessage("§7" + line);
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String alias,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "stats", "debug", "preset", "inspect", "gui", "audit"), args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "debug" -> filter(Arrays.asList("on", "off"), args[1]);
                case "preset" -> filter(Arrays.asList("save", "load", "list", "delete"), args[1]);
                case "inspect" -> filter(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    args[1]
                );
                case "audit" -> filter(Arrays.asList("10", "20", "50", "100"), args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("preset")) {
            String action = args[1].toLowerCase(Locale.ROOT);
            if (action.equals("load") || action.equals("delete")) {
                return filter(new ArrayList<>(config.presetNames()), args[2]);
            }
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String input) {
        String needle = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(needle)) {
                result.add(option);
            }
        }
        return result;
    }
}
