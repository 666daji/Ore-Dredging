package org.oredredging;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.oredredging.client.render.entity.DetonatorEntityRenderer;
import org.oredredging.client.render.model.ModModelLayers;
import org.oredredging.client.render.model.ModModelLoader;
import org.oredredging.client.render.tooltip.MinerBundleTooltipComponent;
import org.oredredging.config.ModConfigs;
import org.oredredging.enchantment.HeavyEnchantment;
import org.oredredging.enchantment.IAttributeModifierEnchantment;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEntities;
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
        MinecraftForge.EVENT_BUS.register(HeavyEnchantment.class);
        MinecraftForge.EVENT_BUS.register(IAttributeModifierEnchantment.class);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            ModConfigs.registerAll();
            LOGGER.info("TW`s Ore Dredging is initializing!");
        }

        @SubscribeEvent
        public static void registerModelLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            ModModelLayers.registryAll(event);
        }

        @SubscribeEvent
        public static void customModelLoading(ModelEvent.RegisterAdditional event) {
            ModModelLoader.initModels(event);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.PEBBLE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.DETONATOR.get(), DetonatorEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.SLIMY_DETONATOR.get(), DetonatorEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.IMPACT_DETONATOR.get(), DetonatorEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registryToolTip(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(MinerBundleItem.MinerBundleTooltipData.class, MinerBundleTooltipComponent::new);
        }
    }

    public static ResourceLocation createResourceLocation(String nameSpace, String path) {
        return new ResourceLocation(nameSpace, path);
    }
}
