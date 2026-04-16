package org.oredredging.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.oredredging.registry.ModSoundEvent;
import org.oredredging.util.RandomUtil;

public class GravelPilesBlock extends FallingBlock {
    public static final IntegerProperty SHAPE = IntegerProperty.create("shape", 1, 5);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape VOXEL_SHAPE = Block.box(2, 0, 2, 14, 6, 14);

    public GravelPilesBlock(Properties properties) {
        super(properties);
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
        return random.nextInt(5) + 1;
    }

    public int getMaxShapeCount() {
        return 5;
    }

    public IntegerProperty getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(2.0F, 20);
        if (RandomUtil.randomBoolean(0.15F)) {
            entity.dropItem = false;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState downState = world.getBlockState(pos.below());
        return !downState.canBeReplaced();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FACING);
    }

    @Override
    public void onLand(Level world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
        super.onLand(world, pos, fallingBlockState, currentStateInPos, fallingBlockEntity);
        world.playSound(null, pos, ModSoundEvent.PILES_FALL.get(), SoundSource.BLOCKS, 0.5F, 10.0F);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}