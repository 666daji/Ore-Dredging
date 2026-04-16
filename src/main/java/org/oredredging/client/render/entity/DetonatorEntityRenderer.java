package org.oredredging.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.oredredging.entity.AbstractDetonatorEntity;
import com.mojang.math.Axis;

@OnlyIn(Dist.CLIENT)
public class DetonatorEntityRenderer extends ThrownItemRenderer<AbstractDetonatorEntity> {
    private final Font font;
    private final ItemRenderer itemRenderer;
    private final EntityRendererProvider.Context context;

    public DetonatorEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
        this.font = context.getFont();
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AbstractDetonatorEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        boolean shouldRenderItem = entity.tickCount >= 2 ||
                !(this.context.getEntityRenderDispatcher().camera.getEntity().distanceToSqr(entity) < 12.25);
        if (shouldRenderItem) {
            poseStack.pushPose();
            poseStack.scale(2.0F, 2.0F, 2.0F);
            poseStack.translate(0.0F, 0.13F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));

            itemRenderer.renderStatic(
                    entity.getItem(),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    entity.level(),
                    entity.getId()
            );

            poseStack.popPose();
        }

        int igniteTime = entity.getIgniteTime();
        if (igniteTime <= 0) return;

        // 计算剩余秒数（向上取整）
        int seconds = (igniteTime + 19) / 20;
        String text = String.valueOf(seconds);

        // 获取文字宽度用于居中
        int textWidth = this.font.width(text);
        poseStack.pushPose();

        // 将文字位置移动到实体上方
        poseStack.translate(0, entity.getBbHeight() + 0.3F, 0);

        // 使文字始终面向玩家（billboard效果）
        poseStack.mulPose(this.context.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        float scale = 0.025F;
        poseStack.scale(scale, scale, scale);

        // 绘制文字
        Matrix4f matrix = poseStack.last().pose();
        this.font.drawInBatch(
                text,
                -textWidth / 2.0F,
                -4.0F,
                0xFF0000,
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        poseStack.popPose();
    }
}