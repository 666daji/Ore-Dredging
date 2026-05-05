package org.oredredging.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
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
            if (world.isReceivingRedstonePower(pos)) {
                world.scheduleBlockTick(pos, this, 4);
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Explosion explosion = world.createExplosion(
                null, pos.getX(), pos.getY(), pos.getZ(), getPower(state, random), World.ExplosionSourceType.MOB
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
    }

    protected abstract float getPower(BlockState state, Random random);
}
