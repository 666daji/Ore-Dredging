package org.oredredging.client.render.block;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.oredredging.block.ThunderSmelterPipeBlock;
import org.oredredging.block.entity.ThunderSmelterPipeBlockEntity;

public class ThunderSmelterPipeBlockEntityRenderer implements BlockEntityRenderer<ThunderSmelterPipeBlockEntity> {
    protected final ItemRenderer itemRenderer;

    // 每一层物品沿主要堆叠方向的位移增量
    private static final double INPUT_STACK_BACK = 0.03;   // 输入槽物品每层向后移动的距离
    private static final double OUTPUT_STACK_UP = 0.03;   // 输出槽物品每层向上移动的距离

    // 物品模型缩放比例
    private static final float INPUT_SCALE  = 0.5F;
    private static final float OUTPUT_SCALE = 0.3F;

    // 每层绕Y轴的固定旋转增量 (度)
    private static final float INPUT_ROT_Y_INCREMENT  = 8.0F;
    private static final float OUTPUT_ROT_Y_INCREMENT = 10.0F;

    // 输入物品中心位置
    private static final double INPUT_BASE_X = 0.0;
    private static final double INPUT_BASE_Y = -0.25;
    private static final double INPUT_BASE_Z = -0.15;
    // 主输出物品中心位置
    private static final double OUTPUT1_BASE_X = -0.13;
    private static final double OUTPUT1_BASE_Y = -0.35;
    private static final double OUTPUT1_BASE_Z = 0.05;
    // 额外输出物品中心位置
    private static final double OUTPUT2_BASE_X = 0.13;
    private static final double OUTPUT2_BASE_Y = -0.35;
    private static final double OUTPUT2_BASE_Z = 0.05;

    public ThunderSmelterPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThunderSmelterPipeBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Direction facing = entity.getCachedState().get(ThunderSmelterPipeBlock.FACING);
        ItemStack input   = entity.getStack(0);
        ItemStack output1 = entity.getStack(1);
        ItemStack output2 = entity.getStack(2);

        if (input.isEmpty() && output1.isEmpty() && output2.isEmpty()) {
            return;
        }

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        float angle = switch (facing) {
            case EAST  -> 90.0F;
            case SOUTH -> 0.0F;
            case WEST  -> 270.0F;
            default    -> 180.0F; // NORTH
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

        // === 输入槽 (0) ===
        if (!input.isEmpty()) {
            int count = input.getCount();
            for (int i = 0; i < count; i++) {
                matrices.push();
                // 位置：基础位置 + 每层向后偏移
                matrices.translate(
                        INPUT_BASE_X,
                        INPUT_BASE_Y,
                        INPUT_BASE_Z + i * INPUT_STACK_BACK
                );
                // 竖立物品的旋转：仅绕Z轴，每层固定角度增量
                float rotY = i * INPUT_ROT_Y_INCREMENT;
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotY));
                matrices.scale(INPUT_SCALE, INPUT_SCALE, INPUT_SCALE);
                itemRenderer.renderItem(input, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        // === 主输出槽 (1) ===
        if (!output1.isEmpty()) {
            int count = output1.getCount();
            for (int i = 0; i < count; i++) {
                matrices.push();
                // 平放物品，每层向上抬升
                matrices.translate(
                        OUTPUT1_BASE_X,
                        OUTPUT1_BASE_Y + i * OUTPUT_STACK_UP,
                        OUTPUT1_BASE_Z
                );
                // 先做基础平放旋转 (X轴-90度)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                // 再绕Y轴旋转，每层不同角度
                float rotY = i * OUTPUT_ROT_Y_INCREMENT;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
                matrices.scale(OUTPUT_SCALE, OUTPUT_SCALE, OUTPUT_SCALE);
                itemRenderer.renderItem(output1, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        // === 额外输出槽 (2) ===
        if (!output2.isEmpty()) {
            int count = output2.getCount();
            for (int i = 0; i < count; i++) {
                matrices.push();
                matrices.translate(
                        OUTPUT2_BASE_X,
                        OUTPUT2_BASE_Y + i * OUTPUT_STACK_UP,
                        OUTPUT2_BASE_Z
                );
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                float rotY = i * OUTPUT_ROT_Y_INCREMENT;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
                matrices.scale(OUTPUT_SCALE, OUTPUT_SCALE, OUTPUT_SCALE);
                itemRenderer.renderItem(output2, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        matrices.pop();
    }
}