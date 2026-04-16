package org.oredredging.mixin;

import net.minecraft.world.item.ItemStack;
import org.oredredging.registry.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$4")
public class GrindstoneScreenHandlerOutputMixin {

    @Inject(method = "getExperienceFromItem", at = @At("HEAD"), cancellable = true)
    private void getExperience(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.is(ModItems.ARMOR_FRAGMENTS.get())) {
            cir.setReturnValue(1000 * stack.getCount());
        }
    }
}
