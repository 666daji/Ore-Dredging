package org.oredredging;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.oredredging.client.render.model.ModModelLoader;
import org.oredredging.registry.RegistryInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OreDredging.MOD_ID)
public class OreDredging {
    public static final String MOD_ID = "ore_dredging";
    public static final Logger LOGGER = LoggerFactory.getLogger("TW`s Ore Dredging");

    public OreDredging() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RegistryInit.init(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TW`s Ore Dredging is initializing!");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void customModelLoading(ModelEvent.RegisterAdditional event) {
            ModModelLoader.initModels(event);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        }
    }

    public static ResourceLocation createResourceLocation(String nameSpace, String path) {
        return new ResourceLocation(nameSpace, path);
    }
}
