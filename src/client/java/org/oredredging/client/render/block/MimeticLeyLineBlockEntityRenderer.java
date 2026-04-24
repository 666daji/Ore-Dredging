package org.oredredging.client.render.block;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.oredredging.OreDredging;
import org.oredredging.block.entity.MimeticLeyLineBlockEntity;
import org.oredredging.client.registry.ModModelLayers;
import org.oredredging.util.EnhancedAnimationState;

public class MimeticLeyLineBlockEntityRenderer extends WithAnimationBlockEntityRenderer<MimeticLeyLineBlockEntity> {
    private static final Identifier TEXTURE = new Identifier(OreDredging.MOD_ID, "textures/block/entity/mimetic_ley_line.png");

    private final ModelPart all;

    public MimeticLeyLineBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        ModelPart root = context.getLayerModelPart(ModModelLayers.MIMETIC_LEY_LINE);
        this.all = root.getChild("all");
        ModelPart up1 = this.all.getChild("up1");
        ModelPart up2 = this.all.getChild("up2");
        ModelPart up3 = this.all.getChild("up3");
        ModelPart up4 = this.all.getChild("up4");
        ModelPart bellow1 = this.all.getChild("bellow1");
        ModelPart bellow2 = this.all.getChild("bellow2");
        ModelPart bellow3 = this.all.getChild("bellow3");
        ModelPart bellow4 = this.all.getChild("bellow4");
        ModelPart core = this.all.getChild("core");

        registerModelPart("all", all);
        registerModelPart("up1", up1);
        registerModelPart("up2", up2);
        registerModelPart("up3", up3);
        registerModelPart("up4", up4);
        registerModelPart("bellow1", bellow1);
        registerModelPart("bellow2", bellow2);
        registerModelPart("bellow3", bellow3);
        registerModelPart("bellow4", bellow4);
        registerModelPart("core", core);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData all = modelPartData.addChild("all", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 20.2F, 0.0F));

        ModelPartData up1 = all.addChild("up1", ModelPartBuilder.create().uv(8, 0).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(-1.0F, -0.2F, 1.0F));

        ModelPartData up2 = all.addChild("up2", ModelPartBuilder.create().uv(8, 4).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(-1.0F, -0.2F, -1.0F));

        ModelPartData up3 = all.addChild("up3", ModelPartBuilder.create().uv(8, 12).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(1.0F, -0.2F, -1.0F));

        ModelPartData up4 = all.addChild("up4", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(1.0F, -0.2F, 1.0F));

        ModelPartData bellow1 = all.addChild("bellow1", ModelPartBuilder.create().uv(8, 8).cuboid(-2.0F, -0.2F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData bellow2 = all.addChild("bellow2", ModelPartBuilder.create().uv(0, 12).cuboid(0.0F, -0.2F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData bellow3 = all.addChild("bellow3", ModelPartBuilder.create().uv(0, 4).cuboid(-2.0F, -0.2F, 0.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData bellow4 = all.addChild("bellow4", ModelPartBuilder.create().uv(0, 8).cuboid(0.0F, -0.2F, 0.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData core = all.addChild("core", ModelPartBuilder.create().uv(0, 16).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0F)), ModelTransform.pivot(0.0F, -0.2F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void render(MimeticLeyLineBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        if (world == null) return;

        manageAnimationState(entity, tickDelta);

        // 渲染
        matrices.push();
        matrices.translate(0.5, -0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f), 0, 1.1f, 0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.all.render(matrices, vertexConsumer, light, overlay);
        matrices.pop();
    }

    /**
     * 管理动画状态
     */
    private void manageAnimationState(MimeticLeyLineBlockEntity entity, float tickDelta) {
        resetAllModelParts();

        // 获取实体年龄和动画进度
        int age = Math.toIntExact(entity.getProgress());
        float animationProgress = getAnimationProgress(age, tickDelta);

        EnhancedAnimationState animationState = entity.amassAnimationState;
        Animation animation = BlockAnimations.AMASS;

        switch (entity.getState()) {
            case BUDDING -> {
                animationState = entity.buddingAnimationState;
                animation = BlockAnimations.BUDDING;
            }
            case ERUPT -> {
                animationState = entity.eruptAnimationState;
                animation = BlockAnimations.ERUPT;
            }
        }

        alwaysUpdateAnimation(
                animationState,
                animation,
                animationProgress,
                0.5F,
                1.0F
        );
    }

    @Override
    public boolean rendersOutsideBoundingBox(MimeticLeyLineBlockEntity entity) {
        return true;
    }
}
