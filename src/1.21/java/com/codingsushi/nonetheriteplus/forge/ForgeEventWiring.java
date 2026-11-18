package com.codingsushi.nonetheriteplus.forge;

import com.mojang.serialization.MapCodec;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

/** Event wiring for Forge 1.21.4 (EventBus 6, IEventBus + MinecraftForge.EVENT_BUS). */
final class ForgeEventWiring {

    private ForgeEventWiring() {
    }

    static void register(NoNetheritePlusForge mod, DeferredRegister<MapCodec<? extends IGlobalLootModifier>> glmSerializers) {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        glmSerializers.register(modBus);
        modBus.addListener((FMLCommonSetupEvent event) -> mod.commonSetup());

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener((ServerStartedEvent event) -> mod.onServerStarted(event.getServer()));
        forgeBus.addListener((ServerStoppingEvent event) -> mod.onServerStopping());
        forgeBus.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                mod.onServerTickEnd();
            }
        });
        forgeBus.addListener((RegisterCommandsEvent event) -> mod.onRegisterCommands(event.getDispatcher()));
    }
}
