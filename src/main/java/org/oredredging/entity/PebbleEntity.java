package org.oredredging.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.oredredging.config.CanPebbleBreakData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.item.PebbleItem;
import org.oredredging.registry.ModDamageTypes;
import org.oredredging.registry.ModEntities;
import org.oredredging.registry.ModItems;

public class PebbleEntity extends ThrowableItemProjectile {
    public PebbleEntity(EntityType<? extends PebbleEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PebbleEntity(Level level, LivingEntity owner) {
        super(ModEntities.PEBBLE.get(), owner, level);
    }

    public PebbleEntity(Level level, double x, double y, double z) {
        super(ModEntities.PEBBLE.get(), x, y, z, level);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == 3) { // EntityEvent.PROJECTILE_HIT
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
    protected void onHitEntity(EntityHitResult hitResult) {
        float damage = getItemPerformance().hurt();

        if (this.getOwner() instanceof LivingEntity owner) {
            MobEffectInstance strength = owner.getEffect(MobEffects.DAMAGE_BOOST);
            if (strength != null) {
                int amplifier = strength.getAmplifier();
                damage += (amplifier + 1) * 1F;
            }
        }

        Registry<DamageType> damageTypes = level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        hitResult.getEntity().hurt(new DamageSource(damageTypes.getHolderOrThrow(ModDamageTypes.PEBBLE_HIT), this, this.getOwner()), damage);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitResult);
            playBreakEffects(hitResult.getLocation());
            if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            var pos = blockHit.getBlockPos();
            Level level = this.level();
            Vec3 hitPos = hitResult.getLocation();

            if (canBreakBlock(level, pos)) {
                level.destroyBlock(pos, true, this.getOwner());
                level.playSound(null, pos, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1.0F, 10.2F);
            } else {
                playBreakEffects(hitPos);
                if (!level.isClientSide) {
                    this.discard();
                }
            }
        }
    }

    private void playBreakEffects(Vec3 pos) {
        this.level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1.0F, 10.0F);

        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
        }
    }

    private boolean canBreakBlock(Level level, BlockPos pos) {
        CanPebbleBreakData data = ConfigManager.get(ModConfigs.CAN_PEBBLE_BREAK);
        if (data == null) return false;
        if (!(level instanceof WorldGenLevel structureManager)) {
            return false;
        }

        return data.blocks().stream().anyMatch(predicate -> predicate.test(structureManager, pos));
    }

    public PebbleItem.Performance getItemPerformance() {
        if (this.getItem().getItem() instanceof PebbleItem pebbleItem) {
            return pebbleItem.getPerformance();
        }
        return PebbleItem.Performance.STONE;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.STONE_PEBBLE.get();
    }

    @Override
    protected float getGravity() {
        return getItemPerformance().gravity();
    }
}