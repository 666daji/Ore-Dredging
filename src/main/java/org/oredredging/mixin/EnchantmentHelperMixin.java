package org.oredredging.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.oredredging.item.PossibleEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Definition(id = "list", local = @Local(type = List.class))
    @Expression("list")
    @ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private static List<EnchantmentInstance> customPossibleEnchantment(List<EnchantmentInstance> original, int power, ItemStack stack, boolean treasureAllowed) {
        if (stack.getItem() instanceof PossibleEnchantment possibleEnchantment) {
            return possibleEnchantment.modifyList(original, power, stack, treasureAllowed);
        }

        return original;
    }
}