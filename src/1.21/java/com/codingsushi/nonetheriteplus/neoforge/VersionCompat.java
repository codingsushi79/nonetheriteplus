package com.codingsushi.nonetheriteplus.neoforge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Predicate;

/** Minecraft 1.21.4 implementations of APIs that changed in 26.x. */
final class VersionCompat {

    private VersionCompat() {
    }

    static String dimensionId(ServerLevel level) {
        return level.dimension().location().toString();
    }

    static Predicate<CommandSourceStack> requiresOp() {
        return src -> src.hasPermission(2);
    }

    static String packMcmeta(String description) {
        return "{\"pack\":{\"pack_format\":61,\"description\":\"" + description + "\"}}";
    }
}
