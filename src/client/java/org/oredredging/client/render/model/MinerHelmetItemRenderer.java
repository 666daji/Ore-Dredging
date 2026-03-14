package org.oredredging.client.render.model;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import org.oredredging.registry.ModItems;

public class MinerHelmetItemRenderer {
    private static final BuiltinItemRendererRegistry INSTANCE = BuiltinItemRendererRegistry.INSTANCE;

    public static void registryAll() {
        MinerHelmetArmorRenderer renderer = new MinerHelmetArmorRenderer();

        INSTANCE.register(ModItems.MINER_HELMET, renderer::renderItem);
    }
}
