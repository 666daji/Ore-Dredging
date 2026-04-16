package org.oredredging.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.oredredging.item.MinerBundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow
    public abstract ItemStack getResultItem(RegistryAccess registryManager);

    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void craftMinerBundle(CraftingContainer recipeInputInventory, RegistryAccess dynamicRegistryManager, CallbackInfoReturnable<ItemStack> cir) {
        List<ItemStack> stacks = recipeInputInventory.getItems();

        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof MinerBundleItem) {
                CompoundTag nbt = stack.getTag();
                ItemStack result = getResultItem(dynamicRegistryManager).copy();
                result.setTag(nbt);

                cir.setReturnValue(result);
            }
        }
    }
}
