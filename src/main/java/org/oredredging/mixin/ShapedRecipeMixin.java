package org.oredredging.mixin;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
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
    public abstract ItemStack getOutput(DynamicRegistryManager registryManager);

    @Inject(method = "craft(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void craftMinerBundle(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager, CallbackInfoReturnable<ItemStack> cir) {
        List<ItemStack> stacks = recipeInputInventory.getInputStacks();

        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof MinerBundleItem) {
                NbtCompound nbt = stack.getNbt();
                ItemStack result = getOutput(dynamicRegistryManager).copy();
                result.setNbt(nbt);

                cir.setReturnValue(result);
            }
        }
    }
}
