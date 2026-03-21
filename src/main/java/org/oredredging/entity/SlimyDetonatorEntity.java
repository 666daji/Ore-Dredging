package org.oredredging.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class SlimyDetonatorEntity extends AbstractDetonatorEntity {
    private boolean attached;
    private Entity attachedEntity;
    private BlockPos attachedBlockPos;
    private Direction attachedFace;

    public SlimyDetonatorEntity(EntityType<? extends AbstractDetonatorEntity> entityType, World world) {
        super(entityType, world);
    }

    public SlimyDetonatorEntity(World world, LivingEntity owner) {
        super(ModEntities.SLIMY_DETONATOR, world, owner);
    }

    public SlimyDetonatorEntity(World world, double x, double y, double z) {
        super(ModEntities.SLIMY_DETONATOR, world, x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        if (attached) {
            this.setVelocity(Vec3d.ZERO);
            // 确保附着后无重力
            if (!this.hasNoGravity()) {
                this.setNoGravity(true);
            }
        }

        // 检查附着有效性
        if (attached && !this.getWorld().isClient) {
            if (attachedEntity != null) {
                if (!attachedEntity.isAlive() || this.getVehicle() != attachedEntity) {
                    detachAndClear();
                }
            } else if (attachedBlockPos != null) {
                BlockState state = this.getWorld().getBlockState(attachedBlockPos);
                if (!state.isSideSolidFullSquare(this.getWorld(), attachedBlockPos, attachedFace)) {
                    detachAndClear();
                }
            }
        }
    }

    private void detachAndClear() {
        detach();
        this.setNoGravity(false);
        this.attached = false;
        this.attachedEntity = null;
        this.attachedBlockPos = null;
        this.attachedFace = null;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity target = entityHit.getEntity();
            if (target instanceof LivingEntity && !target.isSpectator()) {
                // 先禁用重力，再骑乘
                this.setNoGravity(true);
                if (this.startRiding(target)) {
                    attached = true;
                    attachedEntity = target;
                    attachedBlockPos = null;
                    attachedFace = null;
                    this.setVelocity(Vec3d.ZERO);
                } else {
                    // 骑乘失败则恢复重力
                    this.setNoGravity(false);
                }
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos hitPos = blockHit.getBlockPos();
            Direction side = blockHit.getSide();
            BlockState state = this.getWorld().getBlockState(hitPos);

            if (state.isSideSolidFullSquare(this.getWorld(), hitPos, side)) {
                attached = true;
                attachedBlockPos = hitPos;
                attachedFace = side;
                attachedEntity = null;
                this.setVelocity(Vec3d.ZERO);
                this.setNoGravity(true);
            } else {
                // 非固体表面，反弹
                bounce(blockHit);
            }
        }
    }

    private void bounce(BlockHitResult hit) {
        Vec3d velocity = this.getVelocity();
        Direction side = hit.getSide();

        velocity = switch (side.getAxis()) {
            case X -> new Vec3d(-velocity.x * 0.6, velocity.y * 0.8, velocity.z * 0.8);
            case Y -> new Vec3d(velocity.x * 0.8, -velocity.y * 0.6, velocity.z * 0.8);
            case Z -> new Vec3d(velocity.x * 0.8, velocity.y * 0.8, -velocity.z * 0.6);
        };

        // 正确计算表面外偏移
        Vec3d offset = Vec3d.of(side.getVector()).multiply(0.1);
        Vec3d newPos = hit.getPos().add(offset);

        if (velocity.length() < 0.1) {
            attached = true;
            attachedBlockPos = hit.getBlockPos();
            attachedFace = hit.getSide();
            attachedEntity = null;
            this.setVelocity(Vec3d.ZERO);
            this.setNoGravity(true);
        } else {
            this.setVelocity(velocity);
            this.setPosition(newPos);
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SLIMY_DETONATOR;
    }
}