package org.oredredging.mixin;

import net.minecraft.item.ItemStack;
import org.oredredging.registry.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/screen/GrindstoneScreenHandler$2", "net/minecraft/screen/GrindstoneScreenHandler$3"})
public class GrindstoneScreenHandlerInputMixin {

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void insert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(ModItems.ARMOR_FRAGMENTS)) {
            cir.setReturnValue(true);
        }
    }
}
