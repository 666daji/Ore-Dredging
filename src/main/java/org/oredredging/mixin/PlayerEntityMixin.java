package org.oredredging.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.oredredging.registry.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract float getAttackCooldownProgress(float baseTime);

    @ModifyVariable(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getVelocity()Lnet/minecraft/util/math/Vec3d;"
            ),
            ordinal = 0
    )
    private float modifyCritDamage(float value, Entity target) {
        boolean isCrit = getAttackCooldownProgress(0.5F) > 0.9F
                && fallDistance > 0.0F
                && !isOnGround()
                && !isClimbing()
                && !isTouchingWater()
                && !hasStatusEffect(StatusEffects.BLINDNESS)
                && !hasVehicle()
                && target instanceof LivingEntity
                && !isSprinting();

        int level = EnchantmentHelper.getLevel(ModEnchantments.HEAVY, getStackInHand(Hand.MAIN_HAND));

        if (level > 0 && isCrit) {
            return value * (0.1F * level);
        }

        return value;
    }
}
