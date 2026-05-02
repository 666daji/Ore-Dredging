package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class ImpactDetonatorEntity extends AbstractDetonatorEntity {
    private float power = 8.0F;

    public ImpactDetonatorEntity(EntityType<? extends AbstractDetonatorEntity> entityType, World world) {
        super(entityType, world);
    }

    public ImpactDetonatorEntity(World world, LivingEntity owner) {
        super(ModEntities.IMPACT_DETONATOR, world, owner);
    }

    public ImpactDetonatorEntity(World world, double x, double y, double z) {
        super(ModEntities.IMPACT_DETONATOR, world, x, y, z);
    }

    public void setPower(float power) {
        this.power = power;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        this.explode(power);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.IMPACT_DETONATOR;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.setIgniteTime(-1);
    }
}