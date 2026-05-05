package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class FlameCrystalArrowEntity extends PersistentProjectileEntity {
    public FlameCrystalArrowEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public FlameCrystalArrowEntity(LivingEntity owner, World world) {
        super(ModEntities.FLAME_CRYSTAL_ARROW, owner, world);
    }

    @Override
    protected ItemStack asItemStack() {
        return new ItemStack(ModItems.FLAME_CRYSTAL_ARROW);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            hit();
            this.discard();
        }
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        hit();
    }

    protected void hit() {
        if (!this.getWorld().isClient) {
            this.getWorld().createExplosion(
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    1.0F,
                    World.ExplosionSourceType.MOB
            );
        }

        ThermalCloudEntity cloud = new ThermalCloudEntity(getWorld(), getX(), getY(), getZ(), 3);
        cloud.setDuration(2000);
        cloud.setWaitTime(0);
        cloud.setRadiusGrowth(-0.02F);
        if (getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }
        getWorld().spawnEntity(cloud);
    }
}
