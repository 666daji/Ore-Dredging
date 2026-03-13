package org.oredredging.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import org.oredredging.client.render.model.ModModelLoader;
import org.oredredging.client.render.model.ModRenderLayers;
import org.oredredging.client.render.tooltip.MinerBundleTooltipComponent;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEntities;

public class ModFabricEvents {
    public static void registryAll() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof MinerBundleItem.MinerBundleTooltipData minerData) {
                return new MinerBundleTooltipComponent(minerData);
            }
            return null;
        });

        ModelLoadingPlugin.register(new ModModelLoader());
        EntityRendererRegistry.register(ModEntities.PEBBLE, FlyingItemEntityRenderer::new);
        ModRenderLayers.registryRenderLayer();
    }
}
