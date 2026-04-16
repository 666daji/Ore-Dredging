package org.oredredging.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class SlimyDetonatorEntity extends AbstractDetonatorEntity {
    private boolean attached;
    private Entity attachedEntity;
    private BlockPos attachedBlockPos;
    private Direction attachedFace;

    public SlimyDetonatorEntity(EntityType<? extends AbstractDetonatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SlimyDetonatorEntity(Level level, LivingEntity owner) {
        super(ModEntities.SLIMY_DETONATOR.get(), owner, level);
    }

    public SlimyDetonatorEntity(Level level, double x, double y, double z) {
        super(ModEntities.SLIMY_DETONATOR.get(), x, y, z, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (attached) {
            this.setDeltaMovement(Vec3.ZERO);
            if (!this.isNoGravity()) {
                this.setNoGravity(true);
            }
        }

        if (attached && !this.level().isClientSide) {
            if (attachedEntity != null) {
                if (!attachedEntity.isAlive() || this.getVehicle() != attachedEntity) {
                    detachAndClear();
                }
            } else if (attachedBlockPos != null) {
                BlockState state = this.level().getBlockState(attachedBlockPos);
                if (!state.isFaceSturdy(this.level(), attachedBlockPos, attachedFace)) {
                    detachAndClear();
                }
            }
        }
    }

    private void detachAndClear() {
        unRide();
        this.setNoGravity(false);
        this.attached = false;
        this.attachedEntity = null;
        this.attachedBlockPos = null;
        this.attachedFace = null;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity target = entityHit.getEntity();
            if (target instanceof LivingEntity && !target.isSpectator()) {
                this.setNoGravity(true);
                if (this.startRiding(target)) {
                    attached = true;
                    attachedEntity = target;
                    attachedBlockPos = null;
                    attachedFace = null;
                    this.setDeltaMovement(Vec3.ZERO);
                } else {
                    this.setNoGravity(false);
                }
            }
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos hitPos = blockHit.getBlockPos();
            Direction side = blockHit.getDirection();
            BlockState state = this.level().getBlockState(hitPos);

            if (state.isFaceSturdy(this.level(), hitPos, side)) {
                attached = true;
                attachedBlockPos = hitPos;
                attachedFace = side;
                attachedEntity = null;
                this.setDeltaMovement(Vec3.ZERO);
                this.setNoGravity(true);
            } else {
                bounce(blockHit);
            }
        }
    }

    private void bounce(BlockHitResult hit) {
        Vec3 velocity = this.getDeltaMovement();
        Direction side = hit.getDirection();

        velocity = switch (side.getAxis()) {
            case X -> new Vec3(-velocity.x * 0.6, velocity.y * 0.8, velocity.z * 0.8);
            case Y -> new Vec3(velocity.x * 0.8, -velocity.y * 0.6, velocity.z * 0.8);
            case Z -> new Vec3(velocity.x * 0.8, velocity.y * 0.8, -velocity.z * 0.6);
        };

        Vec3 offset = Vec3.atLowerCornerOf(side.getNormal()).scale(0.1);
        Vec3 newPos = hit.getLocation().add(offset);

        if (velocity.length() < 0.1) {
            attached = true;
            attachedBlockPos = hit.getBlockPos();
            attachedFace = hit.getDirection();
            attachedEntity = null;
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
        } else {
            this.setDeltaMovement(velocity);
            this.setPos(newPos);
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SLIMY_DETONATOR.get();
    }
}