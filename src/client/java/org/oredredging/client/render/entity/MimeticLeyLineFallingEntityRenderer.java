package org.oredredging.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.oredredging.client.registry.ModModelLayers;
import org.oredredging.client.render.block.MimeticLeyLineBlockEntityRenderer;
import org.oredredging.entity.MimeticLeyLineFallingEntity;

public class MimeticLeyLineFallingEntityRenderer extends EntityRenderer<MimeticLeyLineFallingEntity> {
    private final ModelPart all;

    public MimeticLeyLineFallingEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        ModelPart root =  context.getPart(ModModelLayers.MIMETIC_LEY_LINE);
        this.all = root.getChild("all");
        this.shadowRadius = 0.1F;
    }

    @Override
    public void render(MimeticLeyLineFallingEntity fallingEntity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // 手动绑定纹理
        RenderSystem.setShaderTexture(0, MimeticLeyLineBlockEntityRenderer.TEXTURE);

        matrices.push();
        matrices.translate(0, -0.82, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f), 0, 1.1f, 0);

        // 使用正确的 VertexConsumer（带光照和覆盖）
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(MimeticLeyLineBlockEntityRenderer.TEXTURE));
        this.all.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(MimeticLeyLineFallingEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}