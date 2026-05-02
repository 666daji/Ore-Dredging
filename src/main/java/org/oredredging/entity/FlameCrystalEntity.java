package org.oredredging.entity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.oredredging.registry.ModEntities;

public class FlameCrystalEntity extends PebbleEntity{
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
            getWorld().createExplosion(getOwner(), getX(), getY(), getZ(), 3.0F, World.ExplosionSourceType.MOB);
            getWorld().setBlockState(new BlockPos((int) getX(), (int) getY(), (int) getZ()), Blocks.LAVA.getDefaultState(), 3);
            this.discard();
        }
    }
}
