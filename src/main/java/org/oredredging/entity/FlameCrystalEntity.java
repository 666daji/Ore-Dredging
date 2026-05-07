package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.oredredging.block.AbstractFlameCrystalBlock;
import org.oredredging.registry.ModEntities;

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
            AbstractFlameCrystalBlock.createThermalExplosion(
                    getOwner() instanceof LivingEntity explosionOwner? explosionOwner : null,
                    getWorld(), hitResult.getPos(), 3.0F
            );

            this.discard();
        }
    }
}