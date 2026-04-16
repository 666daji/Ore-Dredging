package org.oredredging.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

/**
 * 附魔需要修改物品的属性修饰符
 */
public interface IAttributeModifierEnchantment {
    /**
     * 为指定槽位的物品添加属性修饰符
     * @param stack 物品堆
     * @param slot 装备槽位
     */
    void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, ItemAttributeModifierEvent event);

    @SubscribeEvent
    static void addAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        EquipmentSlot slot = event.getSlotType();

        // 获取物品上的所有附魔
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment instanceof IAttributeModifierEnchantment modifierEnchantment) {
                modifierEnchantment.addAttributeModifiers(stack, slot, event);
            }
        }
    }
}