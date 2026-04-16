package org.oredredging.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.oredredging.item.CollapseStoneHammerItem;
import org.oredredging.registry.ModEnchantments;

public class HeavyEnchantment extends Enchantment {
    public HeavyEnchantment(Rarity weight, EnchantmentCategory target, EquipmentSlot... slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof CollapseStoneHammerItem;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @SubscribeEvent
    public static void modifyCritDamage(CriticalHitEvent event) {
        if (event.isVanillaCritical()) {
            Player player = event.getEntity();

            int level = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.HEAVY.get(), player.getMainHandItem());
            if (level > 0) {
                event.setDamageModifier(event.getDamageModifier() + 0.5F * level);
            }
        }
    }
}
