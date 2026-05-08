package org.oredredging.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.entity.ThunderSmelterPipeBlockEntity;
import org.oredredging.registry.ModBlockEntities;
import org.oredredging.util.RandomUtil;

public class ThunderSmelterPipeBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty CRAFTING = BooleanProperty.of("crafting");
    public static final VoxelShape SHAPE;

    static {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, Block.createCuboidShape(2, 0, 2, 14, 2, 14));
        shape = VoxelShapes.union(shape, Block.createCuboidShape(2, 12, 2, 14, 14, 14));
        shape = VoxelShapes.union(shape, Block.createCuboidShape(2, 0, 2, 4, 14, 14));
        shape = VoxelShapes.union(shape, Block.createCuboidShape(12, 0, 2, 14, 14, 14));
        shape = VoxelShapes.union(shape, Block.createCuboidShape(2, 0, 2, 14, 14, 4));
        shape = VoxelShapes.union(shape, Block.createCuboidShape(2, 0, 12, 14, 14, 14));
        SHAPE = shape;
    }

    public ThunderSmelterPipeBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(CRAFTING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, CRAFTING);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ThunderSmelterPipeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.THUNDER_SMELTER_PIPE, ThunderSmelterPipeBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(CRAFTING)) return;

        int particleCount = random.nextInt(10) + 1;
        for (int i = 0; i < particleCount; i++) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.1;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            world.addParticle(RandomUtil.randomBoolean(0.5F) ? ParticleTypes.SMOKE : ParticleTypes.POOF, x, y, z, 0.0, 0.05, 0.0);
        }

        if (random.nextFloat() < 0.1f) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS,
                    0.4f + random.nextFloat() * 0.3f,
                    0.8f + random.nextFloat() * 0.4f, false);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ThunderSmelterPipeBlockEntity pipe)) {
            return ActionResult.PASS;
        }

        if (pipe.isCrafting()) {
            return ActionResult.PASS;
        }

        // 1. 如果输出槽有任何物品，直接全部取出
        boolean hasOutput = false;
        for (int slot : new int[]{1, 2}) {
            ItemStack stack = pipe.getStack(slot);
            if (!stack.isEmpty()) {
                ItemStack extracted = pipe.removeStack(slot);
                if (!player.getInventory().insertStack(extracted)) {
                    player.dropItem(extracted, false);
                }
                hasOutput = true;
            }
        }
        if (hasOutput) {
            pipe.markDirty();
            return ActionResult.CONSUME;
        }

        // 2. 输出槽为空，但正在熔炼则不允许操作输入槽
        if (pipe.isCrafting()) {
            return ActionResult.PASS;
        }

        ItemStack held = player.getMainHandStack();
        ItemStack inputStack = pipe.getStack(0);

        // 3. 手持物品时的逻辑
        if (!held.isEmpty()) {
            boolean canInsert = inputStack.isEmpty() ||
                    (ItemStack.areItemsEqual(inputStack, held) && inputStack.getCount() < pipe.getSlotMaxCount(0));

            if (canInsert) {
                // 放入一个物品
                ItemStack oneItem = held.copy();
                oneItem.setCount(1);
                held.decrement(1);
                if (inputStack.isEmpty()) {
                    pipe.setStack(0, oneItem);
                } else {
                    inputStack.increment(1);
                }
                pipe.markDirty();
                return ActionResult.CONSUME;
            } else {
                // 无法放入，从输入槽取出一个物品（如果有）
                if (!inputStack.isEmpty()) {
                    ItemStack extracted = pipe.removeStack(0, 1);
                    if (!player.getInventory().insertStack(extracted)) {
                        player.dropItem(extracted, false);
                    }
                    pipe.markDirty();
                    return ActionResult.CONSUME;
                }
                // 输入槽也为空，无法操作
                return ActionResult.PASS;
            }
        } else {
            // 4. 空手时的逻辑
            if (!inputStack.isEmpty()) {
                ItemStack extracted = pipe.removeStack(0, 1);
                if (!player.getInventory().insertStack(extracted)) {
                    player.dropItem(extracted, false);
                }
                pipe.markDirty();
                return ActionResult.CONSUME;
            }
            // 输入槽为空，什么都不做
            return ActionResult.PASS;
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ThunderSmelterPipeBlockEntity pipe) {
                ItemScatterer.spawn(world, pos, pipe);
                world.updateComparators(pos, this);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
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
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ThunderSmelterPipeBlockEntity pipe) {
            float fillFactor = 0.0f;
            for (int i = 0; i < pipe.size(); i++) {
                ItemStack stack = pipe.getStack(i);
                if (!stack.isEmpty()) {
                    fillFactor += (float) stack.getCount() / pipe.getSlotMaxCount(i);
                }
            }
            return (int) (fillFactor / pipe.size() * 14) + (fillFactor > 0 ? 1 : 0);
        }
        return 0;
    }
}