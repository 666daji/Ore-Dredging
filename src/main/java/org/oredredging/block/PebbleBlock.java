package org.oredredging.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * 每个小石子有四种形状，在放置碎石堆时会在四种形状中随机选择一种展示。
 */
public class PebbleBlock extends GravelPilesBlock {
    public static final IntProperty SHAPE = IntProperty.of("shape", 1, 4);
    public static final VoxelShape VOXEL_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 3, 16);

    public PebbleBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        int shape = getShapeFromContext(ctx);
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(SHAPE, shape);
    }

    /**
     * 获取一个随机的碎石子形状。
     *
     * @param ctx 放置上下文
     * @return 随机的形状数字
     */
    private int getShapeFromContext(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        Direction facing = ctx.getHorizontalPlayerFacing();

        // 位置和朝向计算种子，确保客户端和服务端相同
        long seed = pos.hashCode() ^ facing.hashCode();
        Random random = Random.create(seed);
        return random.nextInt(4) + 1;
    }

    @Override
    public int getMaxShapeCount() {
        return 4;
    }

    @Override
    public IntProperty getShape() {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FACING);
    }
}
