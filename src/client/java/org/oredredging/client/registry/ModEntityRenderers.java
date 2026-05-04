package org.oredredging.client.registry;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import org.oredredging.client.render.entity.*;
import org.oredredging.registry.ModEntities;

public class ModEntityRenderers {
    public static void registryAll() {
        EntityRendererRegistry.register(ModEntities.PEBBLE, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLAME_CRYSTAL, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DETONATOR, DetonatorEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SLIMY_DETONATOR, DetonatorEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.IMPACT_DETONATOR, DetonatorEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLAME_CRYSTAL_ARROW, FlameCrystalArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.MIMETIC_LEY_LINE_FALLING, MimeticLeyLineFallingEntityRenderer::new);
    }
}
