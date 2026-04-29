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

    public ThunderSmelterPipeBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThunderSmelterPipeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Direction facing = entity.getCachedState().get(ThunderSmelterPipeBlock.FACING);
        // 获取三个槽位的物品
        ItemStack input = entity.getStack(0);
        ItemStack output1 = entity.getStack(1);
        ItemStack output2 = entity.getStack(2);

        if (input.isEmpty() && output1.isEmpty() && output2.isEmpty()) {
            return;
        }

        matrices.push();

        // 移动到方块中心
        matrices.translate(0.5, 0.5, 0.5);

        // 根据方块朝向旋转
        float angle = switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F; // NORTH
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

        // --- 渲染输入物品（竖立，居中偏上） ---
        if (!input.isEmpty()) {
            matrices.push();
            matrices.translate(0.0, 0.15, 0.0);
            matrices.scale(0.5F, 0.5F, 0.5F);
            itemRenderer.renderItem(input, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }

        // --- 渲染主输出物品（平放在左前下方） ---
        if (!output1.isEmpty()) {
            matrices.push();
            matrices.translate(-0.2, -0.35, 0.15);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            matrices.scale(0.5F, 0.5F, 0.5F);
            itemRenderer.renderItem(output1, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }

        // --- 渲染额外输出物品（平放在右前下方） ---
        if (!output2.isEmpty()) {
            matrices.push();
            matrices.translate(0.2, -0.35, 0.15);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            matrices.scale(0.5F, 0.5F, 0.5F);
            itemRenderer.renderItem(output2, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }

        matrices.pop();
    }
}