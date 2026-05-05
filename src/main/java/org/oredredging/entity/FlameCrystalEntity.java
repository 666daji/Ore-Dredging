package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.oredredging.registry.ModEntities;

import java.util.List;

public class FlameCrystalEntity extends PebbleEntity {
    public FlameCrystalEntity(EntityType<? extends PebbleEntity> entityType, World world) {
        super(entityType, world);
    }

    public FlameCrystalEntity(World world, PlayerEntity user) {
        super(ModEntities.FLAME_CRYSTAL, world, user);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient) {
            // 先引发爆炸，并获取 Explosion 实例
            Explosion explosion = getWorld().createExplosion(
                    getOwner(), getX(), getY(), getZ(), 3.0F, World.ExplosionSourceType.MOB
            );

            List<BlockPos> affected = explosion.getAffectedBlocks();
            double maxDist = 0;
            double cx = getX(), cy = getY(), cz = getZ();
            for (BlockPos pos : affected) {
                double dist = Math.sqrt(pos.getSquaredDistance(cx, cy, cz));
                if (dist > maxDist) maxDist = dist;
            }
            float radius = (float) Math.max(maxDist, 3.0);

            // 生成高温残留云
            ThermalCloudEntity cloud = new ThermalCloudEntity(getWorld(), cx, cy, cz, radius);
            cloud.setDuration(2000);
            cloud.setWaitTime(0);
            cloud.setRadiusGrowth(-0.02F);
            if (getOwner() instanceof LivingEntity owner) {
                cloud.setOwner(owner);
            }
            getWorld().spawnEntity(cloud);

            this.discard(); // 移除自身
        }
    }
}