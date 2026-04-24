package org.oredredging.client.registry;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import org.oredredging.client.particle.MimeticLeyLineDustParticle;
import org.oredredging.client.render.model.*;
import org.oredredging.client.render.tooltip.MinerBundleTooltipComponent;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModItems;
import org.oredredging.registry.ModParticleTypes;

public class ModFabricEvents {
    public static void registryAll() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof MinerBundleItem.MinerBundleTooltipData minerData) {
                return new MinerBundleTooltipComponent(minerData);
            }
            return null;
        });

        ModelLoadingPlugin.register(new ModModelLoader());
        ArmorRenderer.register(new MinerHelmetArmorRenderer(), ModItems.MINER_HELMET);
    }

    public static void init() {
        ModModelLayers.registerAll();
        ModRenderLayers.registryRenderLayer();
        MinerHelmetItemRenderer.registryAll();
        ModEntityRenderers.registryAll();
        registryAll();

        ParticleFactoryRegistry.getInstance().register(ModParticleTypes.MIMETIC_LEY_LINE_DUST, new MimeticLeyLineDustParticle.Factory());
    }
}
