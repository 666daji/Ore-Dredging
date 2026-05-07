package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.oredredging.block.AbstractFlameCrystalBlock;
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
            AbstractFlameCrystalBlock.createThermalExplosion(
                    getOwner() instanceof LivingEntity explosionOwner? explosionOwner : null,
                    getWorld(), getPos(), 1.0F
            );
        }
    }
}
