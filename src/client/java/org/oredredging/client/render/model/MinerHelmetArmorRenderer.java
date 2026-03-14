package org.oredredging.client.render.model;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.oredredging.OreDredging;
import org.oredredging.client.render.ModFabricEvents;

public class MinerHelmetArmorRenderer implements ArmorRenderer {
    private static final Identifier TEXTURE = new Identifier(OreDredging.MOD_ID, "textures/armor/miner_helmet.png");

    private ModelPart bone;

    public MinerHelmetArmorRenderer() {}

    // 模型数据定义
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // 根骨骼 "bone"
        ModelPartData bone = root.addChild("bone",
                ModelPartBuilder.create()
                        .uv(0, 21).cuboid(-4.5F, -5.0F, -4.5F, 9.0F, 2.0F, 1.0F, new Dilation(0.0F))
                        .uv(20, 21).cuboid(-4.5F, -5.0F, 4.5F, 9.0F, 2.0F, 1.0F, new Dilation(0.0F))
                        .uv(22, 0).cuboid(-3.5F, -7.5F, 3.5F, 8.0F, 3.0F, 1.0F, new Dilation(0.0F))
                        .uv(22, 4).cuboid(-3.5F, -7.5F, -3.5F, 8.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.pivot(-0.5F, 20.0F, -0.5F)); // pivot 接近头部中心

        // 子骨骼 cube_r1
        bone.addChild("cube_r1",
                ModelPartBuilder.create().uv(0, 28).cuboid(-2.0F, -2.0F, -15.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-9.5F, -5.0F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r2
        bone.addChild("cube_r2",
                ModelPartBuilder.create().uv(22, 24).cuboid(-2.0F, -2.0F, -15.0F, 2.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-8.5F, -5.0F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r3
        bone.addChild("cube_r3",
                ModelPartBuilder.create().uv(6, 28).cuboid(-3.0F, -2.0F, -14.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-18.5F, -4.5F, 2.5F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r4
        bone.addChild("cube_r4",
                ModelPartBuilder.create().uv(14, 24).cuboid(-3.0F, -3.0F, -14.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-18.0F, -4.0F, 2.0F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r5
        bone.addChild("cube_r5",
                ModelPartBuilder.create().uv(24, 15).cuboid(-2.0F, -4.0F, -14.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-17.5F, -4.8F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r6
        bone.addChild("cube_r6",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-2.0F, -4.0F, -16.0F, 2.0F, 2.0F, 9.0F, new Dilation(0.0F))
                        .uv(0, 11).cuboid(-4.0F, -2.7F, -14.0F, 6.0F, 1.0F, 6.0F, new Dilation(0.0F))
                        .uv(24, 11).cuboid(-4.0F, -2.7F, -8.0F, 6.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-10.5F, -4.8F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r7
        bone.addChild("cube_r7",
                ModelPartBuilder.create().uv(0, 24).cuboid(-4.0F, -3.0F, -8.0F, 6.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-3.5F, -4.5F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // 子骨骼 cube_r8
        bone.addChild("cube_r8",
                ModelPartBuilder.create().uv(0, 18).cuboid(-5.0F, -2.0F, -8.0F, 10.0F, 2.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(12.5F, -3.0F, 0.5F, 0.0F, 1.5708F, 0.0F));

        // 子骨骼 cube_r9
        bone.addChild("cube_r9",
                ModelPartBuilder.create().uv(22, 8).cuboid(-4.0F, -2.0F, -8.0F, 8.0F, 2.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(3.5F, -3.0F, 0.5F, 0.0F, 1.5708F, 0.0F));

        // 子骨骼 bone2
        ModelPartData bone2 = bone.addChild("bone2",
                ModelPartBuilder.create()
                        .uv(2, 43).cuboid(-3.5F, -3.0F, 4.6F, 2.0F, 6.0F, 0.0F, new Dilation(0.0F))
                        .uv(2, 36).cuboid(-3.5F, -3.0F, -3.4F, 2.0F, 6.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // bone2 的子骨骼 cube_r10
        bone2.addChild("cube_r10",
                ModelPartBuilder.create().uv(14, 43).cuboid(-1.0F, -3.0F, 1.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.of(-2.5F, 4.7F, 1.7F, 1.5708F, 0.0F, 0.0F));

        // bone2 的子骨骼 cube_r11
        bone2.addChild("cube_r11",
                ModelPartBuilder.create().uv(2, 43).cuboid(-1.0F, -5.0F, 1.0F, 2.0F, 8.0F, 0.0F, new Dilation(0.0F)),
                ModelTransform.of(-2.5F, 4.0F, 1.6F, 1.5708F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    protected ModelPart getBone() {
        if (this.bone == null) {
            this.bone = MinecraftClient.getInstance().getEntityModelLoader()
                    .getModelPart(ModFabricEvents.MINER_HELMET_LAYER);
        }

        return bone;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        if (slot != EquipmentSlot.HEAD) return;

        matrices.push();

        // 应用头部的旋转
        contextModel.head.rotate(matrices);
        matrices.translate(0.10 / 16.0, -23.0 / 16.0, 0.10 / 16.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));

        // 渲染模型
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE), false, stack.hasGlint());
        getBone().render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);

        matrices.pop();
    }

    public void renderItem(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        // 应用头部的旋转
        matrices.translate(0.5, 1.5f, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));

        // 渲染模型
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE), false, stack.hasGlint());
        getBone().render(matrices, vertexConsumer, light, overlay, 1, 1, 1, 1);

        matrices.pop();
    }
}