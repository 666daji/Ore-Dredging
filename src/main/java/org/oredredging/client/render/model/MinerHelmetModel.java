package org.oredredging.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.oredredging.OreDredging;

@OnlyIn(Dist.CLIENT)
public class MinerHelmetModel extends Model {
    public static final ResourceLocation TEXTURE = OreDredging.createResourceLocation(OreDredging.MOD_ID, "textures/armor/miner_helmet.png");
    private static MinerHelmetModel CACHE;

    private final ModelPart bone;

    public MinerHelmetModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.bone = root.getChild("bone");
    }

    /**
     * 创建模型层定义（用于注册）
     */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 根骨骼 "bone"
        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(0, 21).addBox(-4.5F, -5.0F, -4.5F, 9.0F, 2.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(20, 21).addBox(-4.5F, -5.0F, 4.5F, 9.0F, 2.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(22, 0).addBox(-3.5F, -7.5F, 3.5F, 8.0F, 3.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(22, 4).addBox(-3.5F, -7.5F, -3.5F, 8.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offset(-0.5F, 20.0F, -0.5F));

        // 子骨骼 cube_r1
        bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(0, 28).addBox(-2.0F, -2.0F, -15.0F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-9.5F, -5.0F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // cube_r2
        bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(22, 24).addBox(-2.0F, -2.0F, -15.0F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-8.5F, -5.0F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // cube_r3
        bone.addOrReplaceChild("cube_r3",
                CubeListBuilder.create().texOffs(6, 28).addBox(-3.0F, -2.0F, -14.0F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-18.5F, -4.5F, 2.5F, 0.0F, -1.5708F, 0.0F));

        // cube_r4
        bone.addOrReplaceChild("cube_r4",
                CubeListBuilder.create().texOffs(14, 24).addBox(-3.0F, -3.0F, -14.0F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-18.0F, -4.0F, 2.0F, 0.0F, -1.5708F, 0.0F));

        // cube_r5
        bone.addOrReplaceChild("cube_r5",
                CubeListBuilder.create().texOffs(24, 15).addBox(-2.0F, -4.0F, -14.0F, 2.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-17.5F, -4.8F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // cube_r6
        bone.addOrReplaceChild("cube_r6",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -4.0F, -16.0F, 2.0F, 2.0F, 9.0F, CubeDeformation.NONE)
                        .texOffs(0, 11).addBox(-4.0F, -2.7F, -14.0F, 6.0F, 1.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(24, 11).addBox(-4.0F, -2.7F, -8.0F, 6.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-10.5F, -4.8F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // cube_r7
        bone.addOrReplaceChild("cube_r7",
                CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -3.0F, -8.0F, 6.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-3.5F, -4.5F, 1.5F, 0.0F, -1.5708F, 0.0F));

        // cube_r8
        bone.addOrReplaceChild("cube_r8",
                CubeListBuilder.create().texOffs(0, 18).addBox(-5.0F, -2.0F, -8.0F, 10.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(12.5F, -3.0F, 0.5F, 0.0F, 1.5708F, 0.0F));

        // cube_r9
        bone.addOrReplaceChild("cube_r9",
                CubeListBuilder.create().texOffs(22, 8).addBox(-4.0F, -2.0F, -8.0F, 8.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(3.5F, -3.0F, 0.5F, 0.0F, 1.5708F, 0.0F));

        // bone2
        PartDefinition bone2 = bone.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(2, 43).addBox(-3.5F, -3.0F, 4.6F, 2.0F, 6.0F, 0.0F, CubeDeformation.NONE)
                        .texOffs(2, 36).addBox(-3.5F, -3.0F, -3.4F, 2.0F, 6.0F, 0.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        bone2.addOrReplaceChild("cube_r10",
                CubeListBuilder.create().texOffs(14, 43).addBox(-1.0F, -3.0F, 1.0F, 2.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.5F, 4.7F, 1.7F, 1.5708F, 0.0F, 0.0F));

        bone2.addOrReplaceChild("cube_r11",
                CubeListBuilder.create().texOffs(2, 43).addBox(-1.0F, -5.0F, 1.0F, 2.0F, 8.0F, 0.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.5F, 4.0F, 1.6F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static @NotNull MinerHelmetModel getCache() {
        if (CACHE == null) {
            ModelPart modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.MINER_HELMET_LAYER);
            MinerHelmetModel.CACHE = new MinerHelmetModel(modelPart);
        }

        return CACHE;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float r, float g, float b, float a) {
        poseStack.pushPose();
        poseStack.translate(0.10 / 16.0, -23.0 / 16.0, 0.10 / 16.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));

        bone.render(poseStack, vertexConsumer, light, overlay, r, g, b, a);
        poseStack.popPose();
    }

    public void renderAsItem(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5f, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));

        bone.render(poseStack, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}