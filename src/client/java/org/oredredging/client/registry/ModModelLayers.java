package org.oredredging.client.registry;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.client.render.block.MimeticLeyLineBlockEntityRenderer;
import org.oredredging.client.render.model.MinerHelmetArmorRenderer;

public class ModModelLayers {
    public static final EntityModelLayer MIMETIC_LEY_LINE = registerMain("mimetic_ley_line", MimeticLeyLineBlockEntityRenderer::getTexturedModelData);
    public static final EntityModelLayer MINER_HELMET_LAYER =  registerMain("miner_helmet", MinerHelmetArmorRenderer::getTexturedModelData);

    private static EntityModelLayer registerMain(String id, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        return registerAll(id, "main", provider);
    }

    private static EntityModelLayer registerAll(String id, String layer, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
        EntityModelLayer entityModelLayer = create(id, layer);
        EntityModelLayerRegistry.registerModelLayer(entityModelLayer, provider);
        return entityModelLayer;
    }

    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(new Identifier(OreDredging.MOD_ID, id), layer);
    }

    public static void registerAll() {
    }
}
