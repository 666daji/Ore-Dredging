package org.oredredging.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.oredredging.OreDredging;

public class ModModelLayers {
    public static final ModelLayerLocation MINER_HELMET_LAYER = new ModelLayerLocation(
            OreDredging.createResourceLocation(OreDredging.MOD_ID, "miner_helmet"), "main"
    );

    public static void registryAll(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MINER_HELMET_LAYER, MinerHelmetModel::createLayer);
    }
}

