package org.oredredging.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;

public abstract class AbstractDetonatorEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> IGNITE_TIME = SynchedEntityData.defineId(AbstractDetonatorEntity.class, EntityDataSerializers.INT);

    public AbstractDetonatorEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public AbstractDetonatorEntity(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity owner, Level level) {
        super(entityType, owner, level);
    }

    public AbstractDetonatorEntity(EntityType<? extends ThrowableItemProjectile> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IGNITE_TIME, 0);
    }

    public void setIgniteTime(int ticks) {
        this.entityData.set(IGNITE_TIME, ticks);
    }

    public int getIgniteTime() {
        return this.entityData.get(IGNITE_TIME);
    }

    @Override
    public void tick() {
        super.tick();

        int igniteTime = getIgniteTime();
        if (igniteTime == -1) {
            return;
        }

        if (!this.level().isClientSide) {
            if (igniteTime > 0) {
                setIgniteTime(igniteTime - 1);
            }
            if (igniteTime <= 0) {
                explode();
            }
        } else if (igniteTime > 0) {
            spawnPrimeParticle(igniteTime);
        }
    }

    protected void spawnPrimeParticle(int igniteTime) {
        final double LOCAL_X = -0.3;
        final double LOCAL_Y = 0.2;
        final double LOCAL_Z = 0.0;

        float yaw = this.getYRot();
        double rad = Math.toRadians(yaw);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double worldDx = LOCAL_X * cos + LOCAL_Z * sin;
        double worldDz = LOCAL_Z * cos - LOCAL_X * sin;
        double worldDy = LOCAL_Y;

        double px = getX() + worldDx;
        double py = getY() + worldDy;
        double pz = getZ() + worldDz;

        int particleCount = Math.min(10, (int)(20.0 / (igniteTime + 1)) + 1);
        for (int i = 0; i < particleCount; i++) {
            double spread = 0.05;
            double rx = px + (random.nextDouble() - 0.5) * spread;
            double ry = py + (random.nextDouble() - 0.5) * spread;
            double rz = pz + (random.nextDouble() - 0.5) * spread;

            double vx = (random.nextDouble() - 0.5) * 0.08;
            double vy = random.nextDouble() * 0.2 + 0.1;
            double vz = (random.nextDouble() - 0.5) * 0.08;

            this.level().addParticle(ParticleTypes.FLAME, rx, ry, rz, vx, vy, vz);

            if (random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, rx, ry, rz, vx * 0.5, vy * 0.5, vz * 0.5);
            }
        }
    }

    protected void explode() {
        if (this.level().isClientSide) return;

        this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                8.0F, Level.ExplosionInteraction.MOB);
        this.level().broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == 3) {
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextFloat() - 0.5) * 0.08,
                        (this.random.nextFloat() - 0.5) * 0.08,
                        (this.random.nextFloat() - 0.5) * 0.08
                );
            }
        }
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }
}