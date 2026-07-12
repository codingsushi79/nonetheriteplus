package com.codingsushi.nonetheriteplus.audit;

import com.codingsushi.nonetheriteplus.NoNetheritePlus;
import com.codingsushi.nonetheriteplus.config.ConfigService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class AuditLog {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private final NoNetheritePlus plugin;
    private final ConfigService config;
    private final Deque<String> recent = new ArrayDeque<>();
    private static final int MEMORY_LINES = 200;

    public AuditLog(NoNetheritePlus plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void log(String event, String details) {
        if (!config.getBoolean("audit.enabled", false)) {
            return;
        }

        String line = FORMATTER.format(Instant.now()) + " [" + event + "] " + details;
        synchronized (recent) {
            recent.addLast(line);
            while (recent.size() > MEMORY_LINES) {
                recent.removeFirst();
            }
        }

        String fileName = config.getConfig().getString("audit.file", "audit.log");
        File file = new File(plugin.getDataFolder(), fileName);
        try {
            if (!file.exists() && !file.createNewFile()) {
                plugin.getLogger().warning("Could not create audit log file");
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not write audit log: " + e.getMessage());
        }
    }

    public List<String> tail(int lines) {
        int wanted = Math.max(1, Math.min(lines, MEMORY_LINES));
        synchronized (recent) {
            if (!recent.isEmpty()) {
                List<String> all = new ArrayList<>(recent);
                int from = Math.max(0, all.size() - wanted);
                return all.subList(from, all.size());
            }
        }

        String fileName = config.getConfig().getString("audit.file", "audit.log");
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            return Collections.emptyList();
        }

        Deque<String> buffer = new ArrayDeque<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.addLast(line);
                while (buffer.size() > wanted) {
                    buffer.removeFirst();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read audit log: " + e.getMessage());
            return Collections.emptyList();
        }
        return new ArrayList<>(buffer);
    }
}
