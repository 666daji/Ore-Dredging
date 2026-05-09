package org.oredredging.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.explosion.Explosion;
import org.oredredging.registry.ModDamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @ModifyExpressionValue(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;shouldDropItemsOnExplosion(Lnet/minecraft/world/explosion/Explosion;)Z"))
    private boolean explosionNoDrop(boolean original) {
        Explosion self = (Explosion) (Object) this;
        if (self.getDamageSource().isOf(ModDamageTypes.FLAME_CRYSTAL_RED_STONE)) {
            return false;
        }

        return original;
    }
}
