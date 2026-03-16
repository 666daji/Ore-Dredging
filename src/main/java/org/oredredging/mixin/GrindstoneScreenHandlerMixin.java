package org.oredredging.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.oredredging.registry.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin {
    @Shadow
    @Final
    Inventory input;

    @Shadow
    @Final
    private Inventory result;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        ItemStack stack0 = this.input.getStack(0);
        ItemStack stack1 = this.input.getStack(1);
        boolean singleInput = (!stack0.isEmpty() && stack1.isEmpty())
                || (stack0.isEmpty() && !stack1.isEmpty());
        ItemStack inputStack = singleInput ? (stack0.isEmpty() ? stack1 : stack0) : ItemStack.EMPTY;

        if (singleInput && inputStack.isOf(ModItems.ARMOR_FRAGMENTS)) {
            // 输出灰烬
            int count = stack0.getCount();
            this.result.setStack(0, new ItemStack(ModItems.ASHES, count));
            ci.cancel();
        }
    }
}