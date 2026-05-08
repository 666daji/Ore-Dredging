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

    // 每层输出物品向上堆叠的位移增量
    private static final double OUTPUT_STACK_UP = 0.03;

    // 物品模型缩放
    private static final float INPUT_SCALE  = 0.4F;
    private static final float OUTPUT_SCALE = 0.3F;

    // ---- 中心渲染位置（输入槽 & 主输出槽共用） ----
    private static final double CENTER_BASE_X = 0.0;
    private static final double CENTER_BASE_Y = -0.25;
    private static final double CENTER_BASE_Z = 0.0;

    // ---- 额外输出槽外围八个相对偏移 ----
    private static final double[][] EXTRA_OFFSETS = {
            {-0.2, 0.0, -0.2},
            {-0.2, 0.0,  0.0},
            {-0.2, 0.0,  0.2},
            { 0.0, 0.0, -0.2},
            { 0.0, 0.0,  0.2},
            { 0.2, 0.0, -0.2},
            { 0.2, 0.0,  0.0},
            { 0.2, 0.0,  0.2}
    };
    private static final double EXTRA_BASE_Y = -0.35; // 额外输出堆叠的起始高度

    public ThunderSmelterPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    /**
     * 生成与物品、槽位、位置绑定的确定性种子。
     */
    private static long createSeed(BlockPos pos, int slot, ItemStack stack) {
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

        // === 中心物品渲染（输入槽 或 主输出槽） ===
        ItemStack centerStack = ItemStack.EMPTY;
        int centerSlot = -1;
        if (!input.isEmpty()) {
            centerStack = input;
            centerSlot = 0;
        } else if (!output1.isEmpty()) {
            // 主输出槽也使用中心渲染逻辑
            centerStack = output1;
            centerSlot = 1;
        }

        if (!centerStack.isEmpty()) {
            int count = centerStack.getCount();
            long seed = createSeed(pos, centerSlot, centerStack);
            Random rand = new Random(seed);
            for (int i = 0; i < count; i++) {
                matrices.push();
                // 所有物品位于同一位置，无层叠偏移
                matrices.translate(CENTER_BASE_X, CENTER_BASE_Y, CENTER_BASE_Z);
                // 随机 Y 轴旋转（与原输入槽相同）
                float rotY = (rand.nextFloat() + 2) * 360.0F;
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
                matrices.scale(INPUT_SCALE, INPUT_SCALE, INPUT_SCALE);
                itemRenderer.renderItem(centerStack, ModelTransformationMode.FIXED, light, overlay,
                        matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        // === 额外输出槽渲染（散布在外围八个位置） ===
        if (!output2.isEmpty()) {
            int count = output2.getCount();
            long seed = createSeed(pos, 2, output2);
            Random rand = new Random(seed);

            // 记录每个外围格子内已放置的物品数量
            int[] slotCounts = new int[EXTRA_OFFSETS.length];

            for (int i = 0; i < count; i++) {
                matrices.push();

                // 随机选择一个外围格子
                int slotIndex = rand.nextInt(EXTRA_OFFSETS.length);
                double[] offset = EXTRA_OFFSETS[slotIndex];

                // 该物品的 Y 位置 = 基础高度 + 该格子内已有物品堆叠高度
                double y = EXTRA_BASE_Y + slotCounts[slotIndex] * OUTPUT_STACK_UP;
                slotCounts[slotIndex]++;

                matrices.translate(offset[0], y, offset[2]);

                // 保留平放与随机旋转
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                float rotY = rand.nextFloat() * 360.0F;          // 注意：此处绕 Z 轴旋转
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