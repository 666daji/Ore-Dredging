package org.oredredging.mixin;

import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEnchantments;
import org.oredredging.util.RandomUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getPossibleEntries", at = @At(value = "HEAD"), cancellable = true)
    private static void canMinerBundleItem(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (stack.getItem() instanceof MinerBundleItem) {
            List<Enchantment> enchantments = List.of(ModEnchantments.CONVERGENCE, ModEnchantments.AUTO_PICKING, ModEnchantments.EXPANSION);
            List<EnchantmentLevelEntry> list = Lists.newArrayList();

            for (Enchantment enchantment : enchantments) {
                int i = enchantment == ModEnchantments.EXPANSION? RandomUtil.nextInt(2) + 1:1;
                list.add(new EnchantmentLevelEntry(enchantment, i));
            }

            cir.setReturnValue(list);
        }
    }
}
