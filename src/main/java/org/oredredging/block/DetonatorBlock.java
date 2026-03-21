package org.oredredging.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.oredredging.entity.AbstractDetonatorEntity;

public class DetonatorBlock extends Block {
    public static final DirectionProperty FACING = Properties.HOPPER_FACING;
    public static final BooleanProperty UNSTABLE = Properties.UNSTABLE;
    public static final VoxelShape VOXEL_SHAPE_X = Block.createCuboidShape(6, 0, 2, 10, 4, 14);
    public static final VoxelShape VOXEL_SHAPE_Z = Block.createCuboidShape(2, 0, 6, 14, 4, 10);
    public final EntityType<? extends AbstractDetonatorEntity> BASE_DETONATOR;

    public DetonatorBlock(Settings settings, EntityType<? extends AbstractDetonatorEntity> baseDetonator) {
        super(settings);
        this.BASE_DETONATOR = baseDetonator;
        this.setDefaultState(this.getDefaultState()
                .with(FACING, net.minecraft.util.math.Direction.NORTH)
                .with(UNSTABLE, false));
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(UNSTABLE, false);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(FACING).getAxis() == Direction.Axis.X) {
            return VOXEL_SHAPE_X;
        }

        return VOXEL_SHAPE_Z;
    }

    // ====================== 引燃逻辑提取 ======================

    /**
     * 原地引燃雷管，生成一个已点燃的实体（无初速度）
     * @param world 世界
     * @param pos 方块位置
     * @param igniter 点燃者（可为 null）
     */
    protected void primeDetonator(World world, BlockPos pos, @Nullable LivingEntity igniter) {
        if (world.isClient) return;

        AbstractDetonatorEntity entity = BASE_DETONATOR.create(world);
        if (entity != null) {
            entity.setOwner(igniter);
            entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.setIgniteTime(100); // 默认引信时间 100 tick
            world.spawnEntity(entity);
            world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    /**
     * 抛出式引燃（玩家用打火石右键时），生成一个有初速度的实体
     */
    private void throwPrimedDetonator(World world, BlockPos pos, PlayerEntity player) {
        if (world.isClient) return;

        AbstractDetonatorEntity entity = BASE_DETONATOR.create(world);
        if (entity != null) {
            entity.setOwner(player);
            entity.setPosition(pos.toCenterPos());
            // 模拟玩家抛出雷管的效果
            entity.setVelocity(player, 0F, player.getYaw(), 0.5F, 0.05F, 1.0F);
            entity.setIgniteTime(100);
            world.spawnEntity(entity);
            world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            if (world.isReceivingRedstonePower(pos)) {
                primeDetonator(world, pos, null);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            primeDetonator(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    // 玩家用打火石/火焰弹右键
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.FLINT_AND_STEEL) || stack.isOf(Items.FIRE_CHARGE)) {
            if (!world.isClient) {
                // 消耗耐久/物品
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    stack.damage(1, player, p -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                } else {
                    stack.decrement(1);
                }
                throwPrimedDetonator(world, pos, player);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    // 被爆炸摧毁时
    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (world.isClient) return;

        primeDetonator(world, pos, explosion.getCausingEntity());
        world.removeBlock(pos, false);
    }

    // 被火矢/火焰弹击中
    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient && projectile.isOnFire() && projectile.canModifyAt(world, hit.getBlockPos())) {
            BlockPos pos = hit.getBlockPos();
            LivingEntity igniter = projectile.getOwner() instanceof LivingEntity ? (LivingEntity) projectile.getOwner() : null;
            primeDetonator(world, pos, igniter);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public boolean shouldDropItemsOnExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, UNSTABLE);
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