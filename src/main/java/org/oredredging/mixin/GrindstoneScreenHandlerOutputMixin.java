package org.oredredging.mixin;

import net.minecraft.item.ItemStack;
import org.oredredging.registry.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerOutputMixin {

    @Inject(method = "getExperience(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void getExperience(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.isOf(ModItems.ARMOR_FRAGMENTS)) {
            cir.setReturnValue(1000 * stack.getCount());
        }
    }
}
