package org.oredredging.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.oredredging.item.CollapseStoneHammerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void excludeItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment self = (Enchantment)(Object)this;
        Set<Enchantment> excludes = Set.of(Enchantments.SHARPNESS, Enchantments.FIRE_ASPECT);

        if (stack.getItem() instanceof CollapseStoneHammerItem && excludes.contains(self)) {
            cir.setReturnValue(false);
        }
    }
}
