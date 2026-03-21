package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class DetonatorEntity extends AbstractDetonatorEntity {
    public DetonatorEntity(EntityType<? extends AbstractDetonatorEntity> entityType, World world) {
        super(entityType, world);
    }

    public DetonatorEntity(World world, LivingEntity owner) {
        super(ModEntities.DETONATOR, world, owner);
    }

    public DetonatorEntity(World world, double x, double y, double z) {
        super(ModEntities.DETONATOR, world, x, y, z);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        // 触碰爆炸类型已经通过 getIgniteTime() == -1 处理，这里只有计时型
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            Vec3d velocity = this.getVelocity();
            Direction side = blockHit.getSide();

            // 反弹并衰减
            velocity = switch (side.getAxis()) {
                case X -> new Vec3d(-velocity.x * 0.6, velocity.y * 0.8, velocity.z * 0.8);
                case Y -> new Vec3d(velocity.x * 0.8, -velocity.y * 0.6, velocity.z * 0.8);
                case Z -> new Vec3d(velocity.x * 0.8, velocity.y * 0.8, -velocity.z * 0.6);
            };

            // 正确计算表面外偏移：在碰撞点坐标上加上法线方向 * 0.1
            Vec3d offset = Vec3d.of(side.getVector()).multiply(0.1);
            Vec3d newPos = blockHit.getPos().add(offset);

            if (velocity.length() < 0.1) {
                this.setVelocity(Vec3d.ZERO);
            } else {
                this.setVelocity(velocity);
                this.setPosition(newPos);
            }
        }

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Vec3d velocity = this.getVelocity();
            this.setVelocity(velocity.multiply(-0.5));
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DETONATOR;
    }
}