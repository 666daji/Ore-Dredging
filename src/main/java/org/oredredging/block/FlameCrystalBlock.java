package org.oredredging.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FlameCrystalBlock extends AbstractFlameCrystalBlock {
    public static final IntProperty COUNT = IntProperty.of("count", 1, 3);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);
    private static final int MAX_COUNT = 3;

    public FlameCrystalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(COUNT, 1));
    }

    @Override
    protected float getPower(BlockState state, Random random) {
        return state.get(COUNT) * 6;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return Objects.requireNonNull(super.getPlacementState(ctx)).with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(COUNT, FACING);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        return super.getDroppedStacks(state, builder)
                .stream()
                .peek(stack -> stack.setCount(state.get(COUNT)))
                .toList();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getStackInHand(hand);
        int currentCount = state.get(COUNT);

        // 情况1：手持本方块对应的物品 → 尝试添加
        if (isFlameCrystalItem(heldStack) && currentCount < MAX_COUNT) {
            if (!world.isClient) {
                BlockState newState = state.with(COUNT, currentCount + 1);
                world.setBlockState(pos, newState, Block.NOTIFY_ALL);

                // 消耗物品 (非创造模式)
                if (!player.isCreative()) {
                    heldStack.decrement(1);
                    player.setStackInHand(hand, heldStack);
                }

                // 播放放置音效
                world.playSound(null, pos, this.soundGroup.getPlaceSound(),
                        SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }
            return ActionResult.success(world.isClient);

            // 情况2：其他情况(手持非对应物品或已达上限) → 尝试取出
        } else if (currentCount > 0) {
            if (!world.isClient) {
                int newCount = currentCount - 1;
                if (newCount > 0) {
                    world.setBlockState(pos, state.with(COUNT, newCount), Block.NOTIFY_ALL);
                } else {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                }

                // 给玩家返还一个物品 (非创造模式)
                if (!player.isCreative()) {
                    ItemStack dropStack = new ItemStack(this.asItem(), 1);
                    if (!player.giveItemStack(dropStack)) {
                        player.dropItem(dropStack, false);
                    }
                }

                // 播放破坏音效
                world.playSound(null, pos, this.soundGroup.getBreakSound(),
                        SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    private boolean isFlameCrystalItem(ItemStack stack) {
        return stack.isOf(this.asItem());
    }
}