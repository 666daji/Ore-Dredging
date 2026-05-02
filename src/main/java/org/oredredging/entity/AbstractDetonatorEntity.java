package org.oredredging.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public abstract class AbstractDetonatorEntity extends ThrownItemEntity {
    private static final TrackedData<Integer> IGNITE_TIME = DataTracker.registerData(AbstractDetonatorEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public AbstractDetonatorEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        this.intersectionChecked = true;
    }

    public AbstractDetonatorEntity(EntityType<? extends ThrownItemEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world);
        this.intersectionChecked = true;
    }

    public AbstractDetonatorEntity(EntityType<? extends ThrownItemEntity> entityType, World world, double x, double y, double z) {
        super(entityType, x, y, z, world);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IGNITE_TIME, 0);
    }

    public void setIgniteTime(int ticks) {
        this.dataTracker.set(IGNITE_TIME, ticks);
    }

    public int getIgniteTime() {
        return this.dataTracker.get(IGNITE_TIME);
    }

    @Override
    public void tick() {
        super.tick();

        int igniteTime = getIgniteTime();
        if (igniteTime == -1) {
            return;
        }

        // 递减引信时间，并在倒计时结束时触发爆炸
        if (!this.getWorld().isClient()) {
            if (igniteTime > 0) {
                setIgniteTime(igniteTime - 1);
            }
            if (igniteTime <= 0) {
                explode(8.0F);
            }
        }

        // 从引线口喷出火花粒子
        else if (igniteTime > 0) {
            spawnPrimeParticle(igniteTime);
        }
    }

    /**
     * 生成引燃时的粒子。
     * <p>剩余时间越少，粒子越多。</p>
     *
     * @param igniteTime 剩余爆炸时间
     */
    protected  void spawnPrimeParticle(int igniteTime) {
        // 局部偏移量
        final double LOCAL_X = -0.3;   // 右侧偏移
        final double LOCAL_Y = 0.2;   // 向上偏移
        final double LOCAL_Z = 0.0;   // 向前偏移

        // 根据实体水平朝向（yaw）旋转局部偏移到世界坐标
        float yaw = this.getYaw();
        double rad = Math.toRadians(yaw);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double worldDx = LOCAL_X * cos + LOCAL_Z * sin;
        double worldDz = LOCAL_Z * cos - LOCAL_X * sin;
        double worldDy = LOCAL_Y;

        double px = getX() + worldDx;
        double py = getY() + worldDy;
        double pz = getZ() + worldDz;

        // 粒子数量随剩余时间减少而增加
        int particleCount = Math.min(10, (int)(20.0 / (igniteTime + 1)) + 1);
        for (int i = 0; i < particleCount; i++) {
            // 在引线口位置添加少量随机散布，避免过于死板
            double spread = 0.05;
            double rx = px + (random.nextDouble() - 0.5) * spread;
            double ry = py + (random.nextDouble() - 0.5) * spread;
            double rz = pz + (random.nextDouble() - 0.5) * spread;

            // 向上飘的速度
            double vx = (random.nextDouble() - 0.5) * 0.08;
            double vy = random.nextDouble() * 0.2 + 0.1;
            double vz = (random.nextDouble() - 0.5) * 0.08;

            this.getWorld().addParticle(ParticleTypes.FLAME, rx, ry, rz, vx, vy, vz);

            // 随机添加烟雾粒子
            if (random.nextInt(3) == 0) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, rx, ry, rz, vx * 0.5, vy * 0.5, vz * 0.5);
            }
        }
    }

    /**
     * 使雷管爆炸。
     */
    protected void explode(float power) {
        if (getWorld().isClient()) {
            return;
        }

        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(),
                power, World.ExplosionSourceType.MOB);
        this.getWorld().sendEntityStatus(this, (byte) 3);
        this.discard();
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 3) {
            for (int i = 0; i < 8; i++) {
                this.getWorld().addParticle(
                        new net.minecraft.particle.ItemStackParticleEffect(net.minecraft.particle.ParticleTypes.ITEM, this.getStack()),
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