package org.oredredging.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.oredredging.item.CollapseStoneHammerItem;

public class HeavyEnchantment extends Enchantment {
    public HeavyEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot... slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof CollapseStoneHammerItem;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMaxPower(int level) {
        return super.getMaxPower(level) + 2;
    }
}
