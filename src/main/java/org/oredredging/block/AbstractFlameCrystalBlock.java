package org.oredredging.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.oredredging.entity.ThermalCloudEntity;

import java.util.List;

public abstract class AbstractFlameCrystalBlock extends Block {
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
        if (!world.isClient) {
            boolean bl = false;
            for (Direction direction : new Direction[]{Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH}) {
                if (world.getBlockState(pos.offset(direction)).isOf(Blocks.REDSTONE_WIRE)) bl = true;
            }

            if (world.isReceivingRedstonePower(pos) || bl) {
                world.scheduleBlockTick(pos, this, 4);
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.createExplosion(
                null, pos.getX(), pos.getY(), pos.getZ(), getPower(state, random), World.ExplosionSourceType.MOB
        );
    }

    protected abstract float getPower(BlockState state, Random random);

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
}
