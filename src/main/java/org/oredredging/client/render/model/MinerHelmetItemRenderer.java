package org.oredredging.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MinerHelmetItemRenderer extends BlockEntityWithoutLevelRenderer {
    public MinerHelmetItemRenderer(BlockEntityRenderDispatcher blockEntityRenderer, EntityModelSet entityModelSet) {
        super(blockEntityRenderer, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        MinerHelmetModel.getCache().renderAsItem(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(MinerHelmetModel.TEXTURE)), light, overlay);
    }
}
