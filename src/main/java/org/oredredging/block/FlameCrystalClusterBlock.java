package org.oredredging.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.entity.FlameCrystalClusterBlockEntity;
import org.oredredging.registry.ModItems;
import org.oredredging.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FlameCrystalClusterBlock extends AbstractFlameCrystalBlock implements Waterloggable, BlockEntityProvider {
    public static final Map<Predicate<ItemStack>, Float> EXPLOSION_PROBABILITY = new HashMap<>();

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = Properties.FACING;

    protected final VoxelShape northShape;
    protected final VoxelShape southShape;
    protected final VoxelShape eastShape;
    protected final VoxelShape westShape;
    protected final VoxelShape upShape;
    protected final VoxelShape downShape;

    public FlameCrystalClusterBlock(Settings settings) {
        super(settings);
        this.upShape = Block.createCuboidShape(3, 0.0, 3, 16 - 3, 4, 16 - 3);
        this.downShape = Block.createCuboidShape(3, 16 - 4, 3, 16 - 3, 16.0, 16 - 3);
        this.northShape = Block.createCuboidShape(3, 3, 16 - 4, 16 - 3, 16 - 3, 16.0);
        this.southShape = Block.createCuboidShape(3, 3, 0.0, 16 - 3, 16 - 3, 4);
        this.eastShape = Block.createCuboidShape(0.0, 3, 3, 4, 16 - 3, 16 - 3);
        this.westShape = Block.createCuboidShape(16 - 4, 3, 3, 16.0, 16 - 3, 16 - 3);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        switch (direction) {
            case NORTH:
                return this.northShape;
            case SOUTH:
                return this.southShape;
            case EAST:
                return this.eastShape;
            case WEST:
                return this.westShape;
            case DOWN:
                return this.downShape;
            case UP:
            default:
                return this.upShape;
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof FlameCrystalClusterBlockEntity blockEntity
                && !blockEntity.isNatural()) {
            player.giveItemStack(new ItemStack(asItem()));
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof FlameCrystalClusterBlockEntity blockEntity) {
            blockEntity.markPlayer();
        }
    }

    @Override
    protected float getPower(BlockState state, Random random) {
        return 12;
    }

    @Override
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        float probability = 0.5F;

        for (Predicate<ItemStack> predicate : EXPLOSION_PROBABILITY.keySet()) {
            if (predicate.test(tool)) {
                probability = EXPLOSION_PROBABILITY.get(predicate);
            }
        }

        if (RandomUtil.randomBoolean(probability)) {
            createThermalExplosion(null, world, pos.toCenterPos(), 12);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, direction);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldAccess worldAccess = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        return this.getDefaultState().with(WATERLOGGED, worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER).with(FACING, ctx.getSide());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FlameCrystalClusterBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    static {
        EXPLOSION_PROBABILITY.put(stack -> stack.getItem() instanceof PickaxeItem, 0.18F);
        EXPLOSION_PROBABILITY.put(stack -> stack.isOf(ModItems.GEOLOGICAL_HAMMER), 0.07F);
    }
}
