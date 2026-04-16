package org.oredredging.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.oredredging.registry.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin {
    @Shadow
    @Final
    Container repairSlots;

    @Shadow
    @Final
    private Container resultSlots;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onUpdateResult(CallbackInfo ci) {
        ItemStack stack0 = this.repairSlots.getItem(0);
        ItemStack stack1 = this.repairSlots.getItem(1);
        boolean singleInput = (!stack0.isEmpty() && stack1.isEmpty())
                || (stack0.isEmpty() && !stack1.isEmpty());
        ItemStack inputStack = singleInput ? (stack0.isEmpty() ? stack1 : stack0) : ItemStack.EMPTY;

        if (singleInput && inputStack.is(ModItems.ARMOR_FRAGMENTS.get())) {
            // 输出灰烬
            int count = stack0.getCount();
            this.resultSlots.setItem(0, new ItemStack(ModItems.ASHES.get(), count));
            ci.cancel();
        }
    }
}