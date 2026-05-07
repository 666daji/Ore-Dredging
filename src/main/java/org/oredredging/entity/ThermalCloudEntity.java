package org.oredredging.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.oredredging.registry.ModEntities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThermalCloudEntity extends Entity {
    private static final TrackedData<Float> RADIUS = DataTracker.registerData(ThermalCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private int duration = 200;          // 总存在时间 (tick)
    private int waitTime = 0;            // 开始伤害前等待时间
    private float radiusGrowth = -0.2f;  // 每 tick 半径变化量
    @Nullable
    private LivingEntity owner;

    private final Map<Entity, Integer> nextHurtTick = new HashMap<>();

    public ThermalCloudEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    /**
     * 创建一个以 (centerX, centerY, centerZ) 为球心的效果云。
     * 实体位置会自动调整到 (centerX, centerY - radius, centerZ)，以保证包围盒正确包裹球体。
     */
    public ThermalCloudEntity(World world, double centerX, double centerY, double centerZ, float radius) {
        this(ModEntities.THERMAL_CLOUD, world);
        this.setPosition(centerX, centerY - radius, centerZ);
        this.setRadius(radius);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(RADIUS, 3.0f);
    }

    public void setRadius(float radius) {
        this.dataTracker.set(RADIUS, Math.max(radius, 0.1f));
    }

    public float getRadius() {
        return this.dataTracker.get(RADIUS);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setRadiusGrowth(float radiusGrowth) {
        this.radiusGrowth = radiusGrowth;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
    }

    @Nullable
    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        float d = this.getRadius() * 2.0f;
        return EntityDimensions.changing(d, d);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (RADIUS.equals(data)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(data);
    }

    /** 获取效果云球心坐标，即 (实体X, 实体Y+半径, 实体Z) */
    public Vec3d getCenter() {
        return new Vec3d(getX(), getY() + getRadius(), getZ());
    }

    @Override
    public void tick() {
        if (getWorld().isClient) {
            clientTick();
        } else {
            serverTick();
        }
    }

    private void serverTick() {
        if (this.age >= this.duration + this.waitTime) {
            this.discard();
            return;
        }

        // 半径随时间变化（注意：半径变化后需要重新计算包围盒和实体位置偏移吗？）
        float radius = this.getRadius();
        if (radiusGrowth != 0.0f) {
            float newRadius = radius + radiusGrowth;
            if (newRadius <= 0.5f) {
                this.discard();
                return;
            }
            // 调整实体Y坐标，保持中心不变
            Vec3d center = getCenter(); // 当前中心
            this.setPosition(center.x, center.y - newRadius, center.z);
            this.setRadius(newRadius);
            radius = newRadius;
        }

        // 等待阶段不处理伤害
        if (this.age < this.waitTime) {
            return;
        }

        // 时间衰减系数
        float elapsedTime = (float)(this.age - this.waitTime);
        float progress = Math.min(elapsedTime / this.duration, 1.0f);

        int minInterval = 1;
        int maxInterval = 5;
        int baseInterval = Math.round(minInterval + (maxInterval - minInterval) * progress);
        float maxDamage = 4.0f;
        float baseDamage = maxDamage * (1.0f - progress);

        // 获取包围盒内所有生物
        Box box = this.getBoundingBox();
        List<LivingEntity> entities = this.getWorld().getEntitiesByClass(LivingEntity.class, box, e -> true);

        Vec3d center = getCenter();
        float rSq = radius * radius;

        for (LivingEntity entity : entities) {
            double dx = entity.getX() - center.x;
            double dy = entity.getY() - center.y;
            double dz = entity.getZ() - center.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > rSq) continue;

            double dist = Math.sqrt(distSq);
            float distRatio = (float)(dist / radius);

            float intervalDistMultiplier = 3.0f;
            float intervalMultiplier = 1.0f + distRatio * intervalDistMultiplier;
            int actualInterval = Math.max(1, (int)(baseInterval * intervalMultiplier));

            int nextTick = nextHurtTick.getOrDefault(entity, 0);
            if (this.age >= nextTick) {
                float damage = baseDamage * (1.0f - distRatio * 0.5f);
                if (damage > 0) {
                    entity.damage(this.getDamageSources().inFire(), damage);
                    entity.setOnFireFor(2);
                }
                nextHurtTick.put(entity, this.age + actualInterval);
            }
        }

        // 清理无效记录
        nextHurtTick.entrySet().removeIf(entry ->
                !entry.getKey().isAlive() || entry.getKey().isRemoved() || entry.getKey().squaredDistanceTo(center) > rSq
        );
    }

    private void clientTick() {
        float radius = this.getRadius();
        int particleCount = Math.min((int)(Math.PI * radius * radius), 200);
        Vec3d center = getCenter();
        for (int i = 0; i < particleCount; i++) {
            // 在球体内均匀随机取一点
            float angle = this.random.nextFloat() * (float) Math.PI * 2.0f;
            float pitch = (this.random.nextFloat() - 0.5f) * (float) Math.PI;
            float r = radius * (float) Math.cbrt(this.random.nextFloat()); // 体积均匀分布
            double px = center.x + MathHelper.cos(angle) * MathHelper.cos(pitch) * r;
            double py = center.y + MathHelper.sin(pitch) * r;
            double pz = center.z + MathHelper.sin(angle) * MathHelper.cos(pitch) * r;

            double vx = (this.random.nextDouble() - 0.5) * 0.05;
            double vy = this.random.nextDouble() * 0.1;
            double vz = (this.random.nextDouble() - 0.5) * 0.05;
            getWorld().addImportantParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.duration = nbt.getInt("Duration");
        this.waitTime = nbt.getInt("WaitTime");
        this.radiusGrowth = nbt.getFloat("RadiusGrowth");
        if (nbt.contains("Radius")) {
            this.setRadius(nbt.getFloat("Radius"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Duration", this.duration);
        nbt.putInt("WaitTime", this.waitTime);
        nbt.putFloat("RadiusGrowth", this.radiusGrowth);
        nbt.putFloat("Radius", this.getRadius());
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {}

    @Override
    public boolean shouldRender(double distance) {
        return distance < 5000;
    }
}