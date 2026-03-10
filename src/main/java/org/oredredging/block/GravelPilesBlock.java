package org.oredredging.block;

import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.oredredging.registry.ModSoundEvent;
import org.oredredging.util.RandomUtil;

/**
 * 每个碎石堆都有五种形状，在放置碎石堆时会在五种形状中随机选择一种展示。
 */
public class GravelPilesBlock extends FallingBlock {
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

    public int getMaxShapeCount() {
        return 5;
    }

    public IntProperty getShape() {
        return SHAPE;
    }

    @Override
    protected void configureFallingBlockEntity(FallingBlockEntity entity) {
        entity.setHurtEntities(2.0F, 20);
        if (RandomUtil.randomBoolean(0.15F)) {
            entity.setDestroyedOnLanding();
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState downState = world.getBlockState(pos.down());
        return !downState.isReplaceable();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, FACING);
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
        super.onLanding(world, pos, fallingBlockState, currentStateInPos, fallingBlockEntity);
        world.playSound(null,
                pos,
                ModSoundEvent.PEBBLE_BREAK,
                SoundCategory.BLOCKS,
                0.5F, 10.0F);
    }
}
