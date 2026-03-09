package org.oredredging.entity;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.oredredging.item.PebbleItem;
import org.oredredging.registry.ModDamageTypes;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;
import org.oredredging.registry.ModSoundEvent;

public class PebbleEntity extends ThrownItemEntity {
    public PebbleEntity(EntityType<? extends PebbleEntity> entityType, World world) {
        super(entityType, world);
    }

    public PebbleEntity(World world, LivingEntity owner) {
        super(ModEntities.PEBBLE, owner, world);
    }

    public PebbleEntity(World world, double x, double y, double z) {
        super(ModEntities.PEBBLE, x, y, z, world);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            for (int i = 0; i < 8; i++) {
                this.getWorld()
                        .addParticle(
                                new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack()),
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                (this.random.nextFloat() - 0.5) * 0.08,
                                (this.random.nextFloat() - 0.5) * 0.08,
                                (this.random.nextFloat() - 0.5) * 0.08
                        );
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        // 基础伤害
        float damage = getItemPerformance().hurt();

        // 如果发射者拥有力量效果，每级增加 1 伤害
        if (this.getOwner() instanceof LivingEntity owner) {
            StatusEffectInstance strength = owner.getStatusEffect(StatusEffects.STRENGTH);
            if (strength != null) {
                int amplifier = strength.getAmplifier();
                damage += (amplifier + 1) * 1F;
            }
        }

        entityHitResult.getEntity().damage(this.getDamageSources().create(ModDamageTypes.PEBBLE_HIT, this, this.getOwner()), damage);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        Vec3d pos = hitResult.getPos();
        this.getWorld().playSound(
                null,
                new BlockPos((int) pos.x, (int) pos.y, (int) pos.z),
                ModSoundEvent.PEBBLE_BREAK,
                SoundCategory.BLOCKS,
                0.5F, 10.0F
        );
        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
            this.discard();
        }
    }

    public PebbleItem.Performance getItemPerformance() {
        if (getItem().getItem() instanceof PebbleItem pebbleItem) {
            return pebbleItem.getPerformance();
        }

        return PebbleItem.Performance.STONE;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.STONE_PEBBLE;
    }

    @Override
    protected float getGravity() {
        return getItemPerformance().gravity();
    }
}