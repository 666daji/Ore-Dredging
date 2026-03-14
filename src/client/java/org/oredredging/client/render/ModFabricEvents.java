package org.oredredging.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.client.render.model.MinerHelmetArmorRenderer;
import org.oredredging.client.render.model.MinerHelmetItemRenderer;
import org.oredredging.client.render.model.ModModelLoader;
import org.oredredging.client.render.model.ModRenderLayers;
import org.oredredging.client.render.tooltip.MinerBundleTooltipComponent;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class ModFabricEvents {
    public static final EntityModelLayer MINER_HELMET_LAYER = new EntityModelLayer(
            new Identifier(OreDredging.MOD_ID, "miner_helmet"), "main"
    );

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

        // 注册模型层，使用渲染器中的静态方法提供模型数据
        EntityModelLayerRegistry.registerModelLayer(MINER_HELMET_LAYER,  MinerHelmetArmorRenderer::getTexturedModelData);

        // 注册盔甲渲染器，指定要渲染的物品
        ArmorRenderer.register(new MinerHelmetArmorRenderer(), ModItems.MINER_HELMET);

        MinerHelmetItemRenderer.registryAll();
    }
}
