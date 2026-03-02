package org.oredredging.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.oredredging.item.MinerBundleItem;

public class MinerBundleEnchantment extends Enchantment {
    public MinerBundleEnchantment(Rarity weight) {
        super(weight, EnchantmentTarget.WEAPON, EquipmentSlot.values());
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof MinerBundleItem;
    }
}
