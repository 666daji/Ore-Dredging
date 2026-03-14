package org.oredredging.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.oredredging.item.CollapseStoneHammerItem;

public class HeavyEnchantment extends Enchantment {
    protected HeavyEnchantment(Rarity weight) {
        super(weight, EnchantmentTarget.WEAPON, EquipmentSlot.values());
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof CollapseStoneHammerItem;
    }
}
