package org.oredredging.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * 每个碎石堆都有五种形状，在放置碎石堆时会在五种形状中随机选择一种展示。
 */
public class GravelPilesBlock extends Block {
    public static final IntProperty SHAPE = IntProperty.of("shape", 1, 5);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final VoxelShape VOXEL_SHAPE = Block.createCuboidShape(2, 0, 2, 14, 6, 14);

    public GravelPilesBlock(Settings settings) {
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
     * 获取一个随机的碎石堆形状。
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
        return random.nextInt(5) + 1;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState downState = world.getBlockState(pos.down());
        return !downState.isReplaceable();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FACING);
    }
}
