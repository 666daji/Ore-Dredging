package org.oredredging.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PebbleBlock extends GravelPilesBlock {
    public static final IntegerProperty SHAPE = IntegerProperty.create("shape", 1, 4);
    public static final VoxelShape VOXEL_SHAPE = Block.box(0, 0, 0, 16, 3, 16);

    public PebbleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SHAPE, 1));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        int shape = getShapeFromContext(ctx);
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(SHAPE, shape);
    }

    private int getShapeFromContext(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        long seed = pos.hashCode() ^ facing.hashCode();
        RandomSource random = RandomSource.create(seed);
        return random.nextInt(4) + 1;
    }

    @Override
    public int getMaxShapeCount() {
        return 4;
    }

    @Override
    public IntegerProperty getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FACING);
    }
}