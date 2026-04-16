package org.oredredging.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class DetonatorEntity extends AbstractDetonatorEntity {
    public DetonatorEntity(EntityType<? extends AbstractDetonatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DetonatorEntity(Level level, LivingEntity owner) {
        super(ModEntities.DETONATOR.get(), owner, level);
    }

    public DetonatorEntity(Level level, double x, double y, double z) {
        super(ModEntities.DETONATOR.get(), x, y, z, level);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            Vec3 velocity = this.getDeltaMovement();
            var side = blockHit.getDirection();

            velocity = switch (side.getAxis()) {
                case X -> new Vec3(-velocity.x * 0.6, velocity.y * 0.8, velocity.z * 0.8);
                case Y -> new Vec3(velocity.x * 0.8, -velocity.y * 0.6, velocity.z * 0.8);
                case Z -> new Vec3(velocity.x * 0.8, velocity.y * 0.8, -velocity.z * 0.6);
            };

            Vec3 offset = Vec3.atLowerCornerOf(side.getNormal()).scale(0.1);
            Vec3 newPos = blockHit.getLocation().add(offset);

            if (velocity.length() < 0.1) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(velocity);
                this.setPos(newPos);
            }
        }

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Vec3 velocity = this.getDeltaMovement();
            this.setDeltaMovement(velocity.scale(-0.5));
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DETONATOR.get();
    }
}