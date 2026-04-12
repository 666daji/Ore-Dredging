package org.oredredging.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.oredredging.entity.AbstractDetonatorEntity;

@Environment(EnvType.CLIENT)
public class DetonatorEntityRenderer extends FlyingItemEntityRenderer<AbstractDetonatorEntity> {
    private final TextRenderer textRenderer;
    private final ItemRenderer itemRenderer1;

    public DetonatorEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, 1.5F, false);
        this.textRenderer = ctx.getTextRenderer();
        this.itemRenderer1 = ctx.getItemRenderer();
    }

    @Override
    public void render(AbstractDetonatorEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        if (entity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25)) {
            matrices.push();
            matrices.scale(2F, 2F, 2F);
            matrices.translate(0F, 0.13F, 0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));

            this.itemRenderer1
                    .renderItem(
                            entity.getStack(), ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId()
                    );

            matrices.pop();
        }

        int igniteTime = entity.getIgniteTime();
        if (igniteTime <= 0) return;

        // 计算剩余秒数
        int seconds = (igniteTime + 19) / 20;
        String text = String.valueOf(seconds);

        // 获取文字宽度用于居中
        int textWidth = this.textRenderer.getWidth(text);
        matrices.push();

        matrices.translate(0, entity.getHeight() + 0.3F, 0);

        // 使文字始终面向玩家
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

        float scale = 0.025F;
        matrices.scale(scale, scale, scale);

        this.textRenderer.draw(
                text,
                -textWidth / 2.0F,
                -4.0F,
                0xFF0000,
                false,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                light
        );

        matrices.pop();
    }
}