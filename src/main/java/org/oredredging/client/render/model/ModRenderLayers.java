package org.oredredging.client.render.model;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import org.oredredging.registry.ModBlocks;

public class ModRenderLayers {
    private static final BlockRenderLayerMap instance = BlockRenderLayerMap.INSTANCE;

    public static void registryRenderLayer() {
        instance.putBlock(ModBlocks.QUARTZ_GLASS, RenderLayer.getTranslucent());
        instance.putBlock(ModBlocks.QUARTZ_GLASS_PANES, RenderLayer.getTranslucent());
    }
}
