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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.entity.ThunderSmelterPipeBlockEntity;
import org.oredredging.registry.ModBlockEntities;

public class ThunderSmelterPipeBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty CRAFTING = BooleanProperty.of("crafting");
    public static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 14, 14);

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

        // 随机生成烟雾粒子
        int particleCount = random.nextInt(2) + 1; // 1~2个
        for (int i = 0; i < particleCount; i++) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.05, 0.0);
        }

        if (random.nextFloat() < 0.1f) {
            world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
                    SoundCategory.BLOCKS,
                    0.4f + random.nextFloat() * 0.3f, // 音量随机 0.4~0.7
                    0.8f + random.nextFloat() * 0.4f  // 音高随机 0.8~1.2
            );
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

        // 潜行交互：一次性取出所有输出物品
        if (player.isSneaking()) {
            boolean changed = false;
            for (int slot : new int[]{1, 2}) {
                ItemStack stack = pipe.getStack(slot);
                if (!stack.isEmpty()) {
                    ItemStack extracted = pipe.removeStack(slot);
                    if (!player.getInventory().insertStack(extracted)) {
                        player.dropItem(extracted, false);
                    }
                    changed = true;
                }
            }
            if (changed) {
                pipe.markDirty();
            }
            return ActionResult.CONSUME;
        }

        // 站立交互
        ItemStack held = player.getMainHandStack();
        ItemStack inputStack = pipe.getStack(0);

        // 情况1：手持物品
        if (!held.isEmpty()) {
            if (inputStack.isEmpty() || (ItemStack.areItemsEqual(inputStack, held) && inputStack.getCount() < 21)) {
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
                // 无法放入，改为取出1个输入槽物品
                ItemStack extracted = pipe.removeStack(0, 1); // 取出1个
                if (!player.getInventory().insertStack(extracted)) {
                    player.dropItem(extracted, false);
                }
                pipe.markDirty();
                return ActionResult.CONSUME;
            }
        }
        // 情况2：空手，直接尝试取出1个输入槽物品
        else {
            if (!inputStack.isEmpty()) {
                ItemStack extracted = pipe.removeStack(0, 1);
                if (!player.getInventory().insertStack(extracted)) {
                    player.dropItem(extracted, false);
                }
                pipe.markDirty();
                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ThunderSmelterPipeBlockEntity pipe) {
                ItemScatterer.spawn(world, pos, pipe);
                // 更新相邻方块（如比较器）
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
}