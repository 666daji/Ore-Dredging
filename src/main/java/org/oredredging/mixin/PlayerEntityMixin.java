package org.oredredging.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.oredredging.registry.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Definition(id = "f", local = @Local(type = float.class, ordinal = 0))
    @Expression("f * 1.5")
    @ModifyExpressionValue(
            method = "attack",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private float modifyCritDamage(float value, Entity target) {
        int level = EnchantmentHelper.getLevel(ModEnchantments.HEAVY, getStackInHand(Hand.MAIN_HAND));
        if (level > 0) {
            return value * (0.1F * level);
        }

        return value;
    }
}
