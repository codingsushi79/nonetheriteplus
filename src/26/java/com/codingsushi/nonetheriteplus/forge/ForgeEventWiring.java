package com.codingsushi.nonetheriteplus.forge;

import com.mojang.serialization.MapCodec;

import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

/** Event wiring for Forge 26.x (EventBus 7: one static BUS per event type). */
final class ForgeEventWiring {

    private ForgeEventWiring() {
    }

    static void register(NoNetheritePlusForge mod, DeferredRegister<MapCodec<? extends IGlobalLootModifier>> glmSerializers) {
        BusGroup modBusGroup = FMLJavaModLoadingContext.get().getModBusGroup();
        glmSerializers.register(modBusGroup);
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(event -> mod.commonSetup());

        ServerStartedEvent.BUS.addListener(event -> mod.onServerStarted(event.getServer()));
        ServerStoppingEvent.BUS.addListener(event -> mod.onServerStopping());
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> mod.onServerTickEnd());
        RegisterCommandsEvent.BUS.addListener(event -> mod.onRegisterCommands(event.getDispatcher()));
    }
}
