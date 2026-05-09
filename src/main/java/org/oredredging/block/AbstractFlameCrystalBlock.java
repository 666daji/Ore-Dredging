package org.oredredging.block;

import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.oredredging.entity.ThermalCloudEntity;
import org.oredredging.registry.ModDamageTypes;

import java.util.List;

public abstract class AbstractFlameCrystalBlock extends Block implements LandingBlock {
    public AbstractFlameCrystalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient) {
            BlockPos blockPos = hit.getBlockPos();
            world.playSound(null, blockPos, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
            world.playSound(null, blockPos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (!world.isClient) {
            if (shouldRedStoneExplosion(world, pos)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                createRedStoneExplosion(world, pos, getPower(state, world.random));
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        world.scheduleBlockTick(pos, this, 5);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        world.scheduleBlockTick(pos, this, 5);
        if (!world.isClient) {
            if (shouldRedStoneExplosion(world, pos)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                createRedStoneExplosion(world, pos, getPower(state, world.random));
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState checkState = getCheckState(state, world, pos);
        boolean canFallThrough = FallingBlock.canFallThrough(checkState);

        if (canFallThrough && pos.getY() >= world.getBottomY()) {
            FallingBlockEntity.spawnFromBlock(world, pos, state);
        }
    }

    /**
     * 获取由红石直接引发的爆炸威力。
     *
     * @param state 当前方块状态
     * @return 爆炸威力
     */
    protected abstract float getPower(BlockState state, Random random);

    /**
     * 获取当前用于支撑的方块状态。
     *
     * @param state 当前方块状态
     * @param world 当前世界
     * @param pos 当前位置
     * @return 用于支撑的方块状态
     */
    protected abstract BlockState getCheckState(BlockState state, ServerWorld world, BlockPos pos);

    /**
     * 计算两个坐标之间的高度差。
     *
     * @param first 高点
     * @param second 低点
     * @return 高度差，不考虑水平方向，可能返回负值
     */
    protected static int getHighDifference(BlockPos first, BlockPos second) {
        return first.getY() - second.getY();
    }

    /**
     * 检查周围是否有可以导致爆炸的红石。
     *
     * @param world 当前世界
     * @param pos 当前坐标
     * @return 是否应该爆炸
     */
    protected static boolean shouldRedStoneExplosion(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState state = world.getBlockState(pos.offset(direction));
            if (state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(Blocks.REDSTONE_WIRE)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 创建一个会产生高温区域的爆炸。
     *
     * @param owner 制造爆炸的实体
     * @param world 爆炸产生的世界
     * @param pos 爆炸产生的位置
     * @param power 爆炸的威力
     * @return 爆炸产生的高温效果云
     */
    public static ThermalCloudEntity createThermalExplosion(@Nullable LivingEntity owner, World world, Vec3d pos, float power) {
        Explosion explosion = world.createExplosion(
                owner, pos.getX(), pos.getY(), pos.getZ(), power, World.ExplosionSourceType.MOB
        );

        List<BlockPos> affected = explosion.getAffectedBlocks();
        double maxDist = 0;
        double cx = pos.getX(), cy = pos.getY(), cz = pos.getZ();
        for (BlockPos ePos : affected) {
            double dist = Math.sqrt(ePos.getSquaredDistance(cx, cy, cz));
            if (dist > maxDist) maxDist = dist;
        }
        float radius = (float) Math.max(maxDist, 3.0);

        ThermalCloudEntity cloud = new ThermalCloudEntity(world, pos.getX(), pos.getY(), pos.getZ(), radius);
        cloud.setDuration(2000);
        cloud.setWaitTime(0);
        cloud.setRadiusGrowth(-0.02F);
        world.spawnEntity(cloud);

        return cloud;
    }

    public static Explosion createRedStoneExplosion(World world, BlockPos pos, float power) {
        return world.createExplosion(
                null, world.getDamageSources().create(ModDamageTypes.FLAME_CRYSTAL_RED_STONE), null, pos.toCenterPos(), power, false, World.ExplosionSourceType.MOB
        );
    }
}
