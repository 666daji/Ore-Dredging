package org.oredredging.mixin;

import net.minecraft.world.item.ItemStack;
import org.oredredging.registry.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/world/inventory/GrindstoneMenu$2", "net/minecraft/world/inventory/GrindstoneMenu$3"})
public class GrindstoneScreenHandlerInputMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void insert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModItems.ARMOR_FRAGMENTS.get())) {
            cir.setReturnValue(true);
        }
    }
}
