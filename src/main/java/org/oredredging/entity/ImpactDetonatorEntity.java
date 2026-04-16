package org.oredredging.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class ImpactDetonatorEntity extends AbstractDetonatorEntity {
    public ImpactDetonatorEntity(EntityType<? extends AbstractDetonatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ImpactDetonatorEntity(Level level, LivingEntity owner) {
        super(ModEntities.IMPACT_DETONATOR.get(), owner, level);
    }

    public ImpactDetonatorEntity(Level level, double x, double y, double z) {
        super(ModEntities.IMPACT_DETONATOR.get(), x, y, z, level);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        this.explode();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.IMPACT_DETONATOR.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.setIgniteTime(-1);
    }
}