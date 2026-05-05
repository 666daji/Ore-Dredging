package org.oredredging.client.render.block;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.oredredging.block.ThunderSmelterPipeBlock;
import org.oredredging.block.entity.ThunderSmelterPipeBlockEntity;

import java.util.Random;

public class ThunderSmelterPipeBlockEntityRenderer implements BlockEntityRenderer<ThunderSmelterPipeBlockEntity> {
    protected final ItemRenderer itemRenderer;

    // 每层输出物品向上堆叠的位移增量（保留）
    private static final double OUTPUT_STACK_UP = 0.03;

    // 物品模型缩放
    private static final float INPUT_SCALE  = 0.3F;
    private static final float OUTPUT_SCALE = 0.3F;

    // 输入物品固定中心位置
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

    /**
     * 生成与物品、槽位、位置绑定的确定性种子。
     * 当物品类型或数量改变时种子变化，从而刷新随机旋转；否则保持不变。
     */
    private static long createSeed(BlockPos pos, int slot, ItemStack stack) {
        // 使用物品的哈希码（单例唯一），物品数量和槽位构造种子
        return pos.asLong() ^ ((long) stack.getItem().hashCode() << 32)
                ^ ((long) stack.getCount() << 16) ^ slot;
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

        BlockPos pos = entity.getPos();

        // === 输入槽 (0) ===
        if (!input.isEmpty()) {
            int count = input.getCount();
            long seed = createSeed(pos, 0, input);
            Random rand = new Random(seed);
            for (int i = 0; i < count; i++) {
                matrices.push();
                // 所有物品位于同一位置，无层叠偏移
                matrices.translate(INPUT_BASE_X, INPUT_BASE_Y, INPUT_BASE_Z);
                // 随机 Y 轴旋转
                float rotY = (rand.nextFloat() + 2) * 360.0F;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
                matrices.scale(INPUT_SCALE, INPUT_SCALE, INPUT_SCALE);
                itemRenderer.renderItem(input, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        // === 主输出槽 (1) ===
        if (!output1.isEmpty()) {
            int count = output1.getCount();
            long seed = createSeed(pos, 1, output1);
            Random rand = new Random(seed);
            for (int i = 0; i < count; i++) {
                matrices.push();
                // 保留逐层向上堆叠效果
                matrices.translate(
                        OUTPUT1_BASE_X,
                        OUTPUT1_BASE_Y + i * OUTPUT_STACK_UP,
                        OUTPUT1_BASE_Z
                );
                // 基础平放（绕 X 轴 -90°）
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                // 随机 Y 轴旋转（每个物品独立）
                float rotY = rand.nextFloat() * 360.0F;
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotY));
                matrices.scale(OUTPUT_SCALE, OUTPUT_SCALE, OUTPUT_SCALE);
                itemRenderer.renderItem(output1, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        // === 额外输出槽 (2) ===
        if (!output2.isEmpty()) {
            int count = output2.getCount();
            long seed = createSeed(pos, 2, output2);
            Random rand = new Random(seed);
            for (int i = 0; i < count; i++) {
                matrices.push();
                matrices.translate(
                        OUTPUT2_BASE_X,
                        OUTPUT2_BASE_Y + i * OUTPUT_STACK_UP,
                        OUTPUT2_BASE_Z
                );
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                float rotY = rand.nextFloat() * 360.0F;
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotY));
                matrices.scale(OUTPUT_SCALE, OUTPUT_SCALE, OUTPUT_SCALE);
                itemRenderer.renderItem(output2, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        matrices.pop();
    }
}