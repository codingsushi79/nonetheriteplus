package com.codingsushi.nonetheriteplus.neoforge;

import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Predicate;

/** Minecraft 26.x implementations of APIs that changed since 1.21.4. */
final class VersionCompat {

    private VersionCompat() {
    }

    static String dimensionId(ServerLevel level) {
        return level.dimension().identifier().toString();
    }

    static Predicate<CommandSourceStack> requiresOp() {
        return Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
    }

    static String packMcmeta(String description) {
        String format = "[" + SharedConstants.DATA_PACK_FORMAT_MAJOR + "," + SharedConstants.DATA_PACK_FORMAT_MINOR + "]";
        return "{\"pack\":{\"min_format\":" + format + ",\"max_format\":" + format
            + ",\"description\":\"" + description + "\"}}";
    }
}
